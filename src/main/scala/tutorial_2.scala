
import scalismo.geometry._
import scalismo.common._
import scalismo.mesh.TriangleMesh
import scalismo.transformations._
import scalismo.io.MeshIO
import scalismo.ui.api._

object tutorial_2 extends App{
  scalismo.initialize()
  implicit val rng: scalismo.utils.Random = scalismo.utils.Random(42)

  val ui = ScalismoUI()
  val paolaGroup = ui.createGroup("paola")

  val mesh: TriangleMesh[_3D] = MeshIO.readMesh(new java.io.File("./datasets/Paola.ply")).get
  val meshView = ui.show(paolaGroup, mesh, "Paola")

  // ridid geometric transformations for meshes
  import scalismo.registration.LandmarkRegistration
  // import scalismo.registration.{Transformation, RotationTransform, TranslationTransform, RigidTransformation}

  // Define a flipping transformation in the x axis
  val flipTransform = Transformation((p: Point[_3D]) => Point(-p.x, p.y, p.z))
  // When given a point as an argument, the defined transform will then simply return a new point:
  // test this transformation with a simple point
  val pt: Point[_3D] = flipTransform(Point(1.0, 1.0, 1.0))
  println("Flipped point (1,1,1)= " + pt)


  // rigid transformation is a rotation followed by a translation
  val translation = Translation3D(EuclideanVector3D(100, 0, 0))

  // Slides that point 100 units in the x
  println("Translated point (1,1,1)= " + translation(Point(1.0, 1.0, 1.0)))

  //Rotation is a little weird. It requires 3 Euler angles and a center
  val rotationCenter = Point(0.0, 0.0, 0.0)
  val rotation: Rotation[_3D] = Rotation3D(0f, 3.14f, 0f, rotationCenter)

  // Spin it around the y axis meaning both x and z would change but not y
  println("Rotated point (1,1,1)= " + rotation(Point(1.0, 1.0, 1.0)))

  // Now lets transform a mesh and visualize
  val translatedPaola: TriangleMesh[_3D] = mesh.transform(translation)
  val paolaMeshTranslatedView = ui.show(paolaGroup, translatedPaola, "translatedPaola")

  // We can "compose" transformations meaning combine them.
  val rigidTransform1 = CompositeTransformation(translation, rotation)

  // Or do the same thing using predefined functions
  val rigidTransform2: RigidTransformation[_3D] = TranslationAfterRotation[_3D](translation, rotation)

  // Exercise:
  // Apply the rotation transform to the original mesh of Paola and show the result
  //  val rotatedPaola : TriangleMesh[_3D] = mesh.transform(rotation)
  //  val paolaMeshRotatedView = ui.show(paolaGroup, rotatedPaola, "rotatedPaola")

  // Rigid alignment
  // A task that we need to perform in any shape modelling pipeline, is the rigid alignment of
  // objects; I.e. normalizing the pose of an object with respect to some reference.

  val paolaTransformedGroup = ui.createGroup("paolaTransformed")
  val paolaTransformed = mesh.transform(rigidTransform2)
  ui.show(paolaTransformedGroup, paolaTransformed, "paolaTransformed")

  // This aligns the meshs as best as possible with the original

  // Rigid alignment is easiest if we already know some corresponding
  // points in both shapes. Assume for the moment, that we have identified a
  // few corresponding points and marked them using landmarks. We can then apply
  // Procrustes Analysis. Usually, these landmarks would need to be clicked manually
  // in a GUI, saved to disk and then loaded in Scalismo using the methods in LandmarksIO:

  // val landmarks : Seq[Landmark[_3D]] = LandmarkIO.readLandmarksJson3D(new java.io.File("landmarks.json")).get

  // we exploit that the two meshes are the same and hence have the same point ids. We can thus define landmarks programmatically:
  // grab some points
  val ptIds = Seq(PointId(2213), PointId(14727), PointId(8320), PointId(48182))
  // Take these points and find the landmarks on original paola
  val paolaLandmarks = ptIds.map(pId => Landmark(s"lm-${pId.id}", mesh.pointSet.point(pId)))
  // same but find landmarks on transformed paola
  val paolaTransformedLandmarks = ptIds.map(pId => Landmark(s"lm-${pId.id}", paolaTransformed.pointSet.point(pId)))
  // visualise this
  val paolaLandmarkViews = paolaLandmarks.map(lm => ui.show(paolaGroup, lm, s"${lm.id}"))
  val paolaTransformedLandmarkViews = paolaTransformedLandmarks.map(lm => ui.show(paolaTransformedGroup, lm, lm.id))
  // now give these landmarks to a function that best defines the transformation using MSE
  val bestTransform: RigidTransformation[_3D] = LandmarkRegistration.rigid3DLandmarkRegistration(
    paolaLandmarks,
    paolaTransformedLandmarks,
    center = Point(0, 0, 0)
  )
  // test how well it worked
  val transformedLms = paolaLandmarks.map(lm => lm.transform(bestTransform))
  val landmarkViews = ui.show(paolaGroup, transformedLms, "transformedLMs")
  // Now lets do it to the whole mesh
  val alignedPaola = mesh.transform(bestTransform)
  val alignedPaolaView = ui.show(paolaGroup, alignedPaola, "alignedPaola")
  alignedPaolaView.color = java.awt.Color.RED

  // Whats important to note is we have aligned original with translated. What about the reverse?
  // Reverse the transformation
  // val bestTransform2: RigidTransformation[_3D] = LandmarkRegistration.rigid3DLandmarkRegistration(
  //  paolaTransformedLandmarks,
  //  paolaLandmarks, center = Point(0, 0, 0))

  // Now transform the mesh back to the original
  // val alignedPaola2 = paolaTransformed.transform(bestTransform)
  // val alignedPaolaView2 = ui.show(paolaGroup, alignedPaola2, "alignedPaola2")
  // alignedPaolaView2.color = java.awt.Color.BLUE
}
