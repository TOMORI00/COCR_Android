package com.nju.cocr.structure;

import android.graphics.PointF;
import android.graphics.RectF;

import com.nju.cocr.dnn.Recognition;

import java.util.ArrayList;
import java.util.List;

import static com.nju.cocr.structure.Utils.getDistance;

/**
 * 结构综合算法类
 * 1. 检查每个原子和所有键两端的距离，小于阈值，分配原子到键端（显式关系添加）
 * 2、将所有的隐式碳原子找出，添加至atoms,显式关系添加
 * 3、没有被分配到原子的键端看作边缘没写出符号的碳原子，分配原子到键端（显示关系添加）
 */
public class Synthesizer {
    /**
     * 原子序列
     **/
    private List<Atom> atoms;
    /**
     * 化学键序列
     **/
    private List<Bond> bonds;
    /**
     * 连接关系
     **/
    private List<List<Atom>> cons;
    /**
     * 原子方框的平均宽度
     **/
    private double atomAvgWidth;

    public Synthesizer(List<Recognition> recognitions, List<Direction> directions) {
        atoms = new ArrayList<>();
        bonds = new ArrayList<>();
        cons = new ArrayList<>();

        for (Recognition r : recognitions) {
            //化学键的情况
            //用于记录有多少键，方便directions通过下标访问
            int bondIndex = 0;
            if (r.getIndex() <= 4) {
                RectF rect = r.getBoundingBox();

                float startPoint_x;
                float startPoint_y;

                float endPoint_x;
                float endPoint_y;

                //判断朝向来添加端点
                switch (directions.get(bondIndex++)) {
                    case Horizon:
                        startPoint_x = rect.left;
                        startPoint_y = rect.top + rect.height() / 2;
                        endPoint_x = rect.right;
                        endPoint_y = startPoint_y;
                        bonds.add(new Bond(r.getIndex(), r.getBoundingBox(), new PointF(startPoint_x, startPoint_y), new PointF(endPoint_x, endPoint_y)));
                        break;
                    case Vertical:
                        startPoint_x = rect.left + rect.width() / 2;
                        startPoint_y = rect.top;
                        endPoint_x = startPoint_x;
                        endPoint_y = rect.bottom;
                        bonds.add(new Bond(r.getIndex(), r.getBoundingBox(), new PointF(startPoint_x, startPoint_y), new PointF(endPoint_x, endPoint_y)));
                        break;
                    case LeftBottom_RightTop:
                        startPoint_x = rect.left;
                        startPoint_y = rect.bottom;
                        endPoint_x = rect.right;
                        endPoint_y = rect.top;
                        bonds.add(new Bond(r.getIndex(), r.getBoundingBox(), new PointF(startPoint_x, startPoint_y), new PointF(endPoint_x, endPoint_y)));
                        break;
                    case LeftTop_RightBottom:
                        startPoint_x = rect.left;
                        startPoint_y = rect.top;
                        endPoint_x = rect.right;
                        endPoint_y = rect.bottom;
                        bonds.add(new Bond(r.getIndex(), r.getBoundingBox(), new PointF(startPoint_x, startPoint_y), new PointF(endPoint_x, endPoint_y)));
                        break;
                    default:
                        break;
                }

            }
            //原子的情况
            else {
                atoms.add(new Atom(r.getIndex(), r.getBoundingBox()));
            }
        }
        //初始化原子平均长度
        atomAvgWidth = calAtomAvgWidth();
        //初始化cons
        for (int i = 0; i < bonds.size(); i++) {
            List<Atom> innerList = new ArrayList<>();
            innerList.add(null);
            innerList.add(null);
            cons.add(innerList);
        }
    }

    /**
     * 寻找显式原子连接关系
     */
    public void checkExplicitAtom() {
        //阈值设置为平均原子长度
        double threshold = atomAvgWidth;
        for (int i = 0; i < bonds.size(); i++) {
            for (int j = 0; j < atoms.size(); j++) {
                PointF startPoint = bonds.get(i).getStartPoint();
                PointF endPoint = bonds.get(i).getEndPoint();
                double atomToStartPoint = getDistance(startPoint, new PointF(atoms.get(j).getRect().centerX(), atoms.get(j).getRect().centerY()));
                double atomToEndPoint = getDistance(endPoint, new PointF(atoms.get(j).getRect().centerX(), atoms.get(j).getRect().centerY()));
                //如果原子中心到键的start端距离小于阈值
                if (atomToStartPoint < threshold) {
                    //如果此键的start端没有连原子
                    if (cons.get(i).get(0) == null) {
                        //键的start端与此原子建立连接关系
                        cons.get(i).set(0, atoms.get(j));
                    }
                    //如果此键的start的端连有的原子比现在的这个远，说明原本的连接是错误的
                    else if (getDistance(startPoint, new PointF(cons.get(i).get(0).getRect().centerX(), cons.get(i).get(0).getRect().centerY())) > atomToStartPoint) {
                        cons.get(i).set(0, atoms.get(j));
                    }
                }
                //如果原子中心到键的end端距离小于阈值
                if (atomToEndPoint < threshold) {
                    //如果此键的end端没有连原子
                    if (cons.get(i).get(1) == null) {
                        //键的end端与此原子建立连接关系
                        cons.get(i).set(1, atoms.get(j));
                    }
                    //如果此键的end的端连有的原子比现在的这个远，说明原本的连接是错误的
                    else if (getDistance(endPoint, new PointF(cons.get(i).get(1).getRect().centerX(), cons.get(i).get(1).getRect().centerY())) > atomToEndPoint) {
                        cons.get(i).set(1, atoms.get(j));
                    }
                }
            }
        }
        //二次验证
        //对于没有分配到原子的键，阈值直接变大1.5倍，再去判断
        for (int i = 0; i < bonds.size(); i++) {
            threshold = atomAvgWidth;
            if (cons.get(i).get(0) != null || cons.get(i).get(1) != null) {
                continue;
            }
            while (cons.get(i).get(0) == null && cons.get(i).get(1) == null) {
                threshold *= 1.25;
                for (int j = 0; j < atoms.size(); j++) {
                    PointF startPoint = bonds.get(i).getStartPoint();
                    PointF endPoint = bonds.get(i).getEndPoint();
                    double atomToStartPoint = getDistance(startPoint, new PointF(atoms.get(j).getRect().centerX(), atoms.get(j).getRect().centerY()));
                    double atomToEndPoint = getDistance(endPoint, new PointF(atoms.get(j).getRect().centerX(), atoms.get(j).getRect().centerY()));
                    //如果原子中心到键的start端距离小于阈值
                    if (atomToStartPoint < threshold && atomToStartPoint < atomToEndPoint) {
                        //如果此键的start端没有连原子
                        cons.get(i).set(0, atoms.get(j));
                        //如果此键的start的端连有的原子比现在的这个远，说明原本的连接是错误的
                    }
                    //如果原子中心到键的end端距离小于阈值
                    if (atomToEndPoint < threshold && atomToEndPoint < atomToStartPoint) {
                        //键的end端与此原子建立连接关系
                        cons.get(i).set(1, atoms.get(j));
                    }

                }
            }
        }

        //对角线判断，如果夹住直接分配（此处考虑大一点的阈值）
    }

    /**
     * 计算平均原子平均长度
     *
     * @return 原子平均长度
     */
    private double calAtomAvgWidth() {
        double num = 0;
        double sum = 0;
        for (Atom atom : atoms) {
            sum += atom.rect.width();
            num++;
        }
        return sum / num;
    }


    public void findHiddenCarbon() {
        //阈值设置为平均原子宽度的一半，用于判断两个键是否连接
        double threshold = atomAvgWidth / 2;
        for (int i = 0; i < bonds.size(); i++) {
            for (int j = i; j < bonds.size(); j++) {
                double b1Start_to_b2Start = Utils.getDistance(bonds.get(i).getStartPoint(), bonds.get(j).getStartPoint());
                double b1Start_to_b2End = Utils.getDistance(bonds.get(i).getStartPoint(), bonds.get(j).getEndPoint());
                double b1End_to_b2Start = Utils.getDistance(bonds.get(i).getEndPoint(), bonds.get(j).getStartPoint());
                double b1End_to_b2End = Utils.getDistance(bonds.get(i).getEndPoint(), bonds.get(j).getEndPoint());
                //两个键四个端点之间的最短距离
                double minDistance;
                PointF p1 = null;
                PointF p2 = null;
                //判断最小,float类型不能判断相等，所以此处编写较为麻烦
                if (b1Start_to_b2Start < b1Start_to_b2End && b1Start_to_b2Start < b1End_to_b2Start && b1Start_to_b2Start < b1End_to_b2End) {
                    minDistance = b1Start_to_b2Start;
                    p1 = bonds.get(i).getStartPoint();
                    p2 = bonds.get(j).getStartPoint();
                } else if (b1Start_to_b2End < b1Start_to_b2Start && b1Start_to_b2End < b1End_to_b2Start && b1Start_to_b2End < b1End_to_b2End) {
                    minDistance = b1Start_to_b2End;
                    p1 = bonds.get(i).getStartPoint();
                    p2 = bonds.get(j).getEndPoint();
                } else if (b1End_to_b2Start < b1Start_to_b2Start && b1End_to_b2Start < b1Start_to_b2End && b1End_to_b2Start < b1End_to_b2End) {
                    minDistance = b1End_to_b2Start;
                    p1 = bonds.get(i).getEndPoint();
                    p2 = bonds.get(j).getStartPoint();
                } else {
                    minDistance = b1End_to_b2End;
                    p1 = bonds.get(i).getEndPoint();
                    p2 = bonds.get(j).getEndPoint();
                }
                //小于阈值，判定相连，添加碳原子
                if (minDistance < threshold) {
                    PointF centerPoint = new PointF((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
                    RectF rect = new RectF((float) (centerPoint.x - atomAvgWidth / 2), (float) (centerPoint.y - atomAvgWidth / 2), (float) (centerPoint.x + atomAvgWidth / 2), (float) (centerPoint.y + atomAvgWidth / 2));
                    Atom atomPossible = new Atom(5, rect);
                    //判断此可能的隐式碳原子是否是已经存在atoms中的显式碳原子，如果是，则去重
                    boolean alreadyExisted = false;
                    for (Atom atom : atoms) {
                        if (atom.getRect().contains(atomPossible.getRect().centerX(), atomPossible.getRect().centerY())) {
                            alreadyExisted = true;
                        }
                    }
                    if (!alreadyExisted) {
                        atoms.add(atomPossible);
                    }
                }
            }
        }
    }

    /**
     * 添加边缘碳原子
     */
    public void addTerminalCarbon() {
        PointF centerPoint = null;
        for (int i = 0; i < cons.size(); i++) {
            if (cons.get(i).get(0) == null) {
                centerPoint = bonds.get(i).getStartPoint();
                RectF rect = new RectF((float) (centerPoint.x - atomAvgWidth / 2), (float) (centerPoint.y - atomAvgWidth / 2), (float) (centerPoint.x + atomAvgWidth / 2), (float) (centerPoint.y + atomAvgWidth / 2));
                atoms.add(new Atom(5, rect));
            }
            if (cons.get(i).get(1) == null) {
                centerPoint = bonds.get(i).getEndPoint();
                RectF rect = new RectF((float) (centerPoint.x - atomAvgWidth / 2), (float) (centerPoint.y - atomAvgWidth / 2), (float) (centerPoint.x + atomAvgWidth / 2), (float) (centerPoint.y + atomAvgWidth / 2));
                atoms.add(new Atom(5, rect));
            }
        }
    }

    public List<Atom> getAtoms() {
        return atoms;
    }

    public List<Bond> getBonds() {
        return bonds;
    }

    public List<List<Atom>> getCons() {
        return cons;
    }

    public double getAtomAvgWidth() {
        return atomAvgWidth;
    }
}
