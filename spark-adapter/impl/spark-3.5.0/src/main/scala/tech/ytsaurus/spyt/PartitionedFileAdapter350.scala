package tech.ytsaurus.spyt

import org.apache.spark.paths.SparkPath
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.execution.datasources.PartitionedFile
import tech.ytsaurus.spyt.format.{YtPartitionedFile350, YtPartitioningDelegate}
import tech.ytsaurus.spyt.format.YtPartitioningSupport.YtPartitionedFileBase

@MinSparkVersion("3.5.0")
class PartitionedFileAdapter350 extends PartitionedFileAdapter {

  override def createPartitionedFile(partitionValues: InternalRow, filePath: String,
                                     start: Long, length: Long): PartitionedFile = {
    PartitionedFile(partitionValues, SparkPath.fromUrlString(filePath), start, length)
  }

  override def createYtPartitionedFile[T <: YtPartitioningDelegate](delegate: T): YtPartitionedFileBase[T] = {
    new YtPartitionedFile350[T](delegate)
  }
}