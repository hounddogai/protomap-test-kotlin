package com.acme.transport.customs.inspection.profile

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default inspection profiles by code. */
interface DefaultInspectionProfileStore {
    fun find(code: String): DefaultInspectionProfile?
    fun save(item: DefaultInspectionProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultInspectionProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultInspectionProfileStore(
    var items: MutableMap<String, DefaultInspectionProfile>,
    var order: MutableList<String>,
) : DefaultInspectionProfileStore {
    override fun find(code: String): DefaultInspectionProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultInspectionProfile) {
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

    override fun list(): MutableList<DefaultInspectionProfile> {
        val out = mutableListOf<DefaultInspectionProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultInspectionProfileStore(): DefaultInspectionProfileStore {
    return MemoryDefaultInspectionProfileStore(
        mutableMapOf<String, DefaultInspectionProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultInspectionProfileStore(
    var inner: DefaultInspectionProfileStore,
    var cache: TtlCache<DefaultInspectionProfile>,
    var hits: Int,
    var misses: Int,
) : DefaultInspectionProfileStore {
    override fun find(code: String): DefaultInspectionProfile? {
        val hit = (cache.get(code) as DefaultInspectionProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultInspectionProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultInspectionProfile> = inner.list()
}

fun newCachedDefaultInspectionProfileStore(inner: DefaultInspectionProfileStore, ttl: Int, clock: Clock): DefaultInspectionProfileStore {
    return CachedDefaultInspectionProfileStore(
        inner = inner,
        cache = newTtlCache<DefaultInspectionProfile>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default inspection profiles. Empty fields match all. */
data class DefaultInspectionProfileQuery(
    var source: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultInspectionProfile): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultInspectionProfile>): MutableList<DefaultInspectionProfile> {
        val matched = filter(items) { candidate: DefaultInspectionProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultInspectionProfiles(store: DefaultInspectionProfileStore, query: DefaultInspectionProfileQuery): MutableList<DefaultInspectionProfile> {
    return query.apply(store.list())
}
