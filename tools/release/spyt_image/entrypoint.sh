echo "EXTRA_CONFIG_GENERATOR_OPTIONS = $EXTRA_CONFIG_GENERATOR_OPTIONS"
echo "EXTRA_PUBLISH_CLUSTER_OPTIONS = $EXTRA_PUBLISH_CLUSTER_OPTIONS"
echo "EXTRA_SPARK_VERSIONS = $EXTRA_SPARK_VERSIONS"

python3.7 -m scripts.config_generator /data $EXTRA_CONFIG_GENERATOR_OPTIONS
python3.7 -m scripts.publish_cluster /data $EXTRA_PUBLISH_CLUSTER_OPTIONS
python3.7 -m scripts.spark_distrib $EXTRA_SPARK_VERSIONS
