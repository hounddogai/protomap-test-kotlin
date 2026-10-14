package com.acme.transport.safety.cameraevent.eventclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps manual camera event baselines by code. */
interface ManualCameraEventBaselineStore {
    fun find(code: String): ManualCameraEventBaseline?
    fun save(item: ManualCameraEventBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualCameraEventBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualCameraEventBaselineStore(
    var items: MutableMap<String, ManualCameraEventBaseline>,
    var order: MutableList<String>,
) : ManualCameraEventBaselineStore {
    override fun find(code: String): ManualCameraEventBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualCameraEventBaseline) {
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

    override fun list(): MutableList<ManualCameraEventBaseline> {
        val out = mutableListOf<ManualCameraEventBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualCameraEventBaselineStore(): ManualCameraEventBaselineStore {
    return MemoryManualCameraEventBaselineStore(
        mutableMapOf<String, ManualCameraEventBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualCameraEventBaselineStore(
    var inner: ManualCameraEventBaselineStore,
    var cache: TtlCache<ManualCameraEventBaseline>,
    var hits: Int,
    var misses: Int,
) : ManualCameraEventBaselineStore {
    override fun find(code: String): ManualCameraEventBaseline? {
        val hit = (cache.get(code) as ManualCameraEventBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualCameraEventBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualCameraEventBaseline> = inner.list()
}

fun newCachedManualCameraEventBaselineStore(inner: ManualCameraEventBaselineStore, ttl: Int, clock: Clock): ManualCameraEventBaselineStore {
    return CachedManualCameraEventBaselineStore(
        inner = inner,
        cache = newTtlCache<ManualCameraEventBaseline>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual camera event baselines. Empty fields match all. */
data class ManualCameraEventBaselineQuery(
    var name: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualCameraEventBaseline): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualCameraEventBaseline>): MutableList<ManualCameraEventBaseline> {
        val matched = filter(items) { candidate: ManualCameraEventBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualCameraEventBaselines(store: ManualCameraEventBaselineStore, query: ManualCameraEventBaselineQuery): MutableList<ManualCameraEventBaseline> {
    return query.apply(store.list())
}
