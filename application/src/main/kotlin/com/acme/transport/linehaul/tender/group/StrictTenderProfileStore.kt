package com.acme.transport.linehaul.tender.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps strict tender profiles by code. */
interface StrictTenderProfileStore {
    fun find(code: String): StrictTenderProfile?
    fun save(item: StrictTenderProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictTenderProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictTenderProfileStore(
    var items: MutableMap<String, StrictTenderProfile>,
    var order: MutableList<String>,
) : StrictTenderProfileStore {
    override fun find(code: String): StrictTenderProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictTenderProfile) {
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

    override fun list(): MutableList<StrictTenderProfile> {
        val out = mutableListOf<StrictTenderProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictTenderProfileStore(): StrictTenderProfileStore {
    return MemoryStrictTenderProfileStore(mutableMapOf<String, StrictTenderProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictTenderProfileStore(
    var inner: StrictTenderProfileStore,
    var cache: TtlCache<StrictTenderProfile>,
    var hits: Int,
    var misses: Int,
) : StrictTenderProfileStore {
    override fun find(code: String): StrictTenderProfile? {
        val hit = (cache.get(code) as StrictTenderProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictTenderProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictTenderProfile> = inner.list()
}

fun newCachedStrictTenderProfileStore(inner: StrictTenderProfileStore, ttl: Int, clock: Clock): StrictTenderProfileStore {
    return CachedStrictTenderProfileStore(
        inner = inner,
        cache = newTtlCache<StrictTenderProfile>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict tender profiles. Empty fields match all. */
data class StrictTenderProfileQuery(
    var label: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictTenderProfile): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictTenderProfile>): MutableList<StrictTenderProfile> {
        val matched = filter(items) { candidate: StrictTenderProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictTenderProfiles(store: StrictTenderProfileStore, query: StrictTenderProfileQuery): MutableList<StrictTenderProfile> {
    return query.apply(store.list())
}
