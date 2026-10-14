package com.acme.transport.crewing.qualification.overrideitem

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps qualification overrides by code. */
interface QualificationOverrideStore {
    fun find(code: String): QualificationOverride?
    fun save(item: QualificationOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<QualificationOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryQualificationOverrideStore(
    var items: MutableMap<String, QualificationOverride>,
    var order: MutableList<String>,
) : QualificationOverrideStore {
    override fun find(code: String): QualificationOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: QualificationOverride) {
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

    override fun list(): MutableList<QualificationOverride> {
        val out = mutableListOf<QualificationOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryQualificationOverrideStore(): QualificationOverrideStore {
    return MemoryQualificationOverrideStore(
        mutableMapOf<String, QualificationOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedQualificationOverrideStore(
    var inner: QualificationOverrideStore,
    var cache: TtlCache<QualificationOverride>,
    var hits: Int,
    var misses: Int,
) : QualificationOverrideStore {
    override fun find(code: String): QualificationOverride? {
        val hit = (cache.get(code) as QualificationOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: QualificationOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<QualificationOverride> = inner.list()
}

fun newCachedQualificationOverrideStore(inner: QualificationOverrideStore, ttl: Int, clock: Clock): QualificationOverrideStore {
    return CachedQualificationOverrideStore(
        inner = inner,
        cache = newTtlCache<QualificationOverride>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects qualification overrides. Empty fields match all. */
data class QualificationOverrideQuery(
    var depot: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: QualificationOverride): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<QualificationOverride>): MutableList<QualificationOverride> {
        val matched = filter(items) { candidate: QualificationOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findQualificationOverrides(store: QualificationOverrideStore, query: QualificationOverrideQuery): MutableList<QualificationOverride> {
    return query.apply(store.list())
}
