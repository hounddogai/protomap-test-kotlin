package com.acme.transport.crewing.dutyperiod.draft

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period manifests by code. */
interface DutyPeriodManifestStore {
    fun find(code: String): DutyPeriodManifest?
    fun save(item: DutyPeriodManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodManifestStore(
    var items: MutableMap<String, DutyPeriodManifest>,
    var order: MutableList<String>,
) : DutyPeriodManifestStore {
    override fun find(code: String): DutyPeriodManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodManifest) {
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

    override fun list(): MutableList<DutyPeriodManifest> {
        val out = mutableListOf<DutyPeriodManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodManifestStore(): DutyPeriodManifestStore {
    return MemoryDutyPeriodManifestStore(mutableMapOf<String, DutyPeriodManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodManifestStore(
    var inner: DutyPeriodManifestStore,
    var cache: TtlCache<DutyPeriodManifest>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodManifestStore {
    override fun find(code: String): DutyPeriodManifest? {
        val hit = (cache.get(code) as DutyPeriodManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodManifest> = inner.list()
}

fun newCachedDutyPeriodManifestStore(inner: DutyPeriodManifestStore, ttl: Int, clock: Clock): DutyPeriodManifestStore {
    return CachedDutyPeriodManifestStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodManifest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period manifests. Empty fields match all. */
data class DutyPeriodManifestQuery(
    var segment: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodManifest): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodManifest>): MutableList<DutyPeriodManifest> {
        val matched = filter(items) { candidate: DutyPeriodManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodManifests(store: DutyPeriodManifestStore, query: DutyPeriodManifestQuery): MutableList<DutyPeriodManifest> {
    return query.apply(store.list())
}
