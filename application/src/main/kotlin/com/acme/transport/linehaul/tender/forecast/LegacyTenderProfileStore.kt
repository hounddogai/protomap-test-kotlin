package com.acme.transport.linehaul.tender.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps legacy tender profiles by code. */
interface LegacyTenderProfileStore {
    fun find(code: String): LegacyTenderProfile?
    fun save(item: LegacyTenderProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyTenderProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyTenderProfileStore(
    var items: MutableMap<String, LegacyTenderProfile>,
    var order: MutableList<String>,
) : LegacyTenderProfileStore {
    override fun find(code: String): LegacyTenderProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyTenderProfile) {
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

    override fun list(): MutableList<LegacyTenderProfile> {
        val out = mutableListOf<LegacyTenderProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyTenderProfileStore(): LegacyTenderProfileStore {
    return MemoryLegacyTenderProfileStore(mutableMapOf<String, LegacyTenderProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyTenderProfileStore(
    var inner: LegacyTenderProfileStore,
    var cache: TtlCache<LegacyTenderProfile>,
    var hits: Int,
    var misses: Int,
) : LegacyTenderProfileStore {
    override fun find(code: String): LegacyTenderProfile? {
        val hit = (cache.get(code) as LegacyTenderProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyTenderProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyTenderProfile> = inner.list()
}

fun newCachedLegacyTenderProfileStore(inner: LegacyTenderProfileStore, ttl: Int, clock: Clock): LegacyTenderProfileStore {
    return CachedLegacyTenderProfileStore(
        inner = inner,
        cache = newTtlCache<LegacyTenderProfile>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy tender profiles. Empty fields match all. */
data class LegacyTenderProfileQuery(
    var region: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyTenderProfile): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyTenderProfile>): MutableList<LegacyTenderProfile> {
        val matched = filter(items) { candidate: LegacyTenderProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyTenderProfiles(store: LegacyTenderProfileStore, query: LegacyTenderProfileQuery): MutableList<LegacyTenderProfile> {
    return query.apply(store.list())
}
