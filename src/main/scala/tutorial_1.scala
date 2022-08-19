
// this extends the app
// make sure you are using adoptopenJDK

// Basic geometric primitives
import scalismo.geometry.{_3D, Point, Point3D} // for working in 3d space
import scalismo.geometry.{EuclideanVector}
import scalismo.geometry.{IntVector, IntVector3D}
import scalismo.geometry.Landmark

import scalismo.common.PointId // to refer to points by id

// Geometric objects
import scalismo.mesh.TriangleMesh
import scalismo.mesh.TriangleId // to refer to triangles by id
import scalismo.image.{DiscreteImage, DiscreteImage3D}
import scalismo.statisticalmodel.PointDistributionModel

// IO Methods
import scalismo.io.ImageIO;
import scalismo.io.StatisticalModelIO
import scalismo.io.{MeshIO, StatisticalModelIO} // MeshIO to read meshes StaModelIO to read stat shape models

// Visualization
import scalismo.ui.api.ScalismoUI
import scalismo.ui.api.LandmarkView

object tutorial_1 extends App {
  // loads all native C++ libraries
  scalismo.initialize()
  // type of randomness to use
  implicit val rng: scalismo.utils.Random = scalismo.utils.Random(42)

  // load an instance of the GUI, named ui
  val ui = ScalismoUI()

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Meshes (surface data)

  // fetching the face
  val mesh: TriangleMesh[_3D] = MeshIO.readMesh(new java.io.File("./datasets/Paola.ply")).get
  // create a new group, add viz to group
  val paolaGroup = ui.createGroup("paola")
  val meshView = ui.show(paolaGroup, mesh, "Paola")
  // 3D triangle mesh consists of a pointSet, which maintains a collection of 3D points and a list of triangle cells
  // first point in the mesh
  println("first point " + mesh.pointSet.point(PointId(0)))
  // first triangles
  println("first cell " + mesh.triangulation.triangle(TriangleId(0)))
  // Instead of visualizing the mesh, we can also display the points forming the mesh.
  val pointCloudView = ui.show(paolaGroup, mesh.pointSet, "pointCloud")

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Points and Vectors

  // define two specific points
  val p1: Point[_3D] = Point3D(4.0, 5.0, 6.0)
  val p2: Point[_3D] = Point3D(1.0, 2.0, 3.0)
  // The difference between two points is a EuclideanVector
  val v1: EuclideanVector[_3D] = Point3D(4.0, 5.0, 6.0) - Point3D(1.0, 2.0, 3.0)
  // The sum of a point with a vector yields a new point
  val p3: Point[_3D] = p1 + v1
  // Points can be converted to vectors:
  val v2: EuclideanVector[_3D] = p1.toVector
  // and from vectors to points
  val v3: Point[_3D] = v1.toPoint
  // compute centre of mass
  val pointList = Seq(
    Point3D(4.0, 5.0, 6.0),
    Point3D(1.0, 2.0, 3.0),
    Point3D(14.0, 15.0, 16.0),
    Point3D(7.0, 8.0, 9.0),
    Point3D(10.0, 11.0, 12.0)
  )
  // use map to turn points into vectors
  val vectors = pointList.map { (p: Point[_3D]) => p.toVector }
  // sum up all vectors in the collection
  val vectorSum = vectors.reduce { (v1, v2) => v1 + v2 }
  // divide the sum by the number of points
  val centerV: EuclideanVector[_3D] = vectorSum * (1.0 / pointList.length)
  // average displacement again as a point in space
  // val center = centerV.toPoint

  println("Center point =  " + centerV)

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Meshes

  // read in statistical model
  val faceModel: PointDistributionModel[_3D, TriangleMesh] = StatisticalModelIO.readStatisticalTriangleMeshModel3D(new java.io.File("./datasets/bfm.h5")).get
  val faceModelView = ui.show(faceModel, "faceModel")

  // sample from a model programmatically
  // val randomFace: TriangleMesh[_3D] = faceModel.sample()

  val randomFace1: TriangleMesh[_3D] = faceModel.sample
  val randomFace2: TriangleMesh[_3D] = faceModel.sample
  val randomFace3: TriangleMesh[_3D] = faceModel.sample

  val meshView1 = ui.show(randomFace1, "rando1")
  val meshView2 = ui.show(randomFace2, "rando2")
  val meshView3 = ui.show(randomFace3, "rando3")

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Landmarks


  // to click a landmark, we need to use a filter
  val matchingLandmarkViews: Seq[LandmarkView] = ui.filter[LandmarkView](paolaGroup, (l: LandmarkView) => l.name == "noseLM")
  val matchingLandmarks: Seq[Landmark[_3D]] = matchingLandmarkViews.map(lmView => lmView.landmark)
  // get id and pos of matched landmarks
  val landmarkId: String = matchingLandmarks.head.id
  val landmarkPosition: Point[_3D] = matchingLandmarks.head.point
}
