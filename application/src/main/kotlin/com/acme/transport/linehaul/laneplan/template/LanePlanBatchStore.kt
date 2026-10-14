package com.acme.transport.linehaul.laneplan.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan batches by code. */
interface LanePlanBatchStore {
    fun find(code: String): LanePlanBatch?
    fun save(item: LanePlanBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanBatchStore(
    var items: MutableMap<String, LanePlanBatch>,
    var order: MutableList<String>,
) : LanePlanBatchStore {
    override fun find(code: String): LanePlanBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanBatch) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<LanePlanBatch> {
        val out = mutableListOf<LanePlanBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanBatchStore(): LanePlanBatchStore {
    return MemoryLanePlanBatchStore(mutableMapOf<String, LanePlanBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanBatchStore(
    var inner: LanePlanBatchStore,
    var cache: TtlCache<LanePlanBatch>,
    var hits: Int,
    var misses: Int,
) : LanePlanBatchStore {
    override fun find(code: String): LanePlanBatch? {
        val hit = (cache.get(code) as LanePlanBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanBatch> = inner.list()
}

fun newCachedLanePlanBatchStore(inner: LanePlanBatchStore, ttl: Int, clock: Clock): LanePlanBatchStore {
    return CachedLanePlanBatchStore(
        inner = inner,
        cache = newTtlCache<LanePlanBatch>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan batches. Empty fields match all. */
data class LanePlanBatchQuery(
    var label: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanBatch): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LanePlanBatch>): MutableList<LanePlanBatch> {
        val matched = filter(items) { candidate: LanePlanBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanBatches(store: LanePlanBatchStore, query: LanePlanBatchQuery): MutableList<LanePlanBatch> {
    return query.apply(store.list())
}
