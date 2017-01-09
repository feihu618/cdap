/*
 * Copyright © 2017 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.metrics.process;

import co.cask.cdap.api.common.Bytes;
import co.cask.cdap.data2.dataset2.lib.table.MetricsTable;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * An abstraction on persistent storage of consumer information.
 */
public abstract class AbstractConsumerMetaTable {
  private static final byte[] VALUE_COLUMN = Bytes.toBytes("o");

  private final MetricsTable metaTable;

  public AbstractConsumerMetaTable(MetricsTable metaTable) {
    this.metaTable = metaTable;
  }

  public synchronized <T> void save(Map<T, Long> offsets) throws Exception {
    SortedMap<byte[], SortedMap<byte[], Long>> updates = Maps.newTreeMap(Bytes.BYTES_COMPARATOR);
    for (Map.Entry<T, Long> entry : offsets.entrySet()) {
      SortedMap<byte[], Long> map = new TreeMap<>(Bytes.BYTES_COMPARATOR);
      map.put(VALUE_COLUMN, entry.getValue());
      updates.put(getKey(entry.getKey()), map);
    }
    metaTable.put(updates);
  }

  public synchronized <T> void save(T key, long value) throws Exception {
    SortedMap<byte[], SortedMap<byte[], Long>> updates = Maps.newTreeMap(Bytes.BYTES_COMPARATOR);
    SortedMap<byte[], Long> map = new TreeMap<>(Bytes.BYTES_COMPARATOR);
    map.put(VALUE_COLUMN, value);
    updates.put(getKey(key), map);
    metaTable.put(updates);
  }

  /**
   * Gets the value as long in the {@link MetricsTable} of a given key.
   *
   * @param objectKey Object form of the key to get value with.
   * @return The value or {@code -1} if the value is not found.
   * @throws Exception If there is an error when fetching.
   */
  public synchronized long get(Object objectKey) throws Exception {
    byte[] result = metaTable.get(getKey(objectKey), VALUE_COLUMN);
    if (result == null) {
      return -1;
    }
    return Bytes.toLong(result);
  }

  protected abstract byte[] getKey(Object objectKey);
}
