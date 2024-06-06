package spyt

import sbt.Keys.baseDirectory
import sbt.{IO, SettingKey, State, StateTransform, TaskKey, ThisBuild}
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations.reapply
import sbtrelease.Utilities._
import sbtrelease.Versions
import spyt.SpytPlugin.autoImport._

import java.io.File

object ReleaseUtils {

  lazy val logSpytVersion: ReleaseStep = { st: State =>
    st.log.info(s"SPYT scala version: ${st.extract.get(spytVersion)}")
    st.log.info(s"SPYT python version: ${st.extract.get(spytPythonVersion)}")
    st
  }

  lazy val dumpVersions: ReleaseStep = { st: State =>
    val scalaVersion = st.extract.get(spytVersion)
    val pythonVersion = st.extract.get(spytPythonVersion)
    st.log.info(s"SPYT scala version dump: $scalaVersion")
    st.log.info(s"SPYT python version dump: $pythonVersion")
    if (!publishYtEnabled) {
      dumpVersionsToBuildDirectory(
        Map("scala" -> scalaVersion, "python" -> pythonVersion),
        st.extract.get(baseDirectory), "version.json"
      )
    }
    st
  }

  def updatePythonVersion(spytPythonVersion: String,
                          spytScalaVersion: String,
                          spytVersionFile: File): Unit = {
    val content =
      s"""# This file is autogenerated, don't edit it manually
         |
         |__version__ = "$spytPythonVersion"
         |__scala_version__ = "$spytScalaVersion"
         |""".stripMargin
    IO.write(spytVersionFile, content)
  }

  def writeVersion(versions: Seq[(SettingKey[String], String)],
                   file: File): Unit = {
    val versionStr =
      s"""import spyt.SpytPlugin.autoImport._
         |
         |${versions.map { case (k, v) => s"""ThisBuild / ${k.key} := "$v"""" }.mkString("\n")}""".stripMargin
    IO.writeLines(file, Seq(versionStr))
  }

  def setVersionForced(spytVersions: Seq[(SettingKey[String], String)],
                       fileSetting: SettingKey[File]): ReleaseStep = { st: State =>
    st.log.info(s"Setting ${spytVersions.map { case (k, v) => s"${k.key} to $v" }.mkString(", ")}")

    val file = st.extract.get(fileSetting)
    writeVersion(spytVersions, file)

    reapply(spytVersions.map { case (k, v) => ThisBuild / k := v }, st)
  }

  def setVersion(versions: SettingKey[Versions],
                 spytVersions: Seq[(SettingKey[String], Versions => String)],
                 fileSetting: SettingKey[File]): ReleaseStep = { st: State =>
    val vs = st.get(versions.key)
      .getOrElse(sys.error("No versions are set! Was this release part executed before inquireVersions?"))
    val selected = spytVersions.map(v => v._1 -> v._2.apply(vs))

    setVersionForced(selected, fileSetting)(st)
  }

  def maybeSetVersion(versions: SettingKey[Versions],
                      spytVersions: Seq[(SettingKey[String], Versions => String)],
                      fileSetting: SettingKey[File]): ReleaseStep = {
    setVersion(versions, spytVersions, fileSetting)
  }

  def runProcess(state: State, process: Seq[ReleaseStep]*): State = {
    process.foldLeft(state) { case (processState, nextProcess) =>
      nextProcess.foldLeft(processState) { case (stepState, nextStep) =>
        StateTransform(nextStep).transform(stepState)
      }
    }
  }
}
