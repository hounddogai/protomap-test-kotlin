package com.acme.transport.crewing.dutyperiod.ledger

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient duty period profiles by code. */
interface LenientDutyPeriodProfileStore {
    fun find(code: String): LenientDutyPeriodProfile?
    fun save(item: LenientDutyPeriodProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientDutyPeriodProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientDutyPeriodProfileStore(
    var items: MutableMap<String, LenientDutyPeriodProfile>,
    var order: MutableList<String>,
) : LenientDutyPeriodProfileStore {
    override fun find(code: String): LenientDutyPeriodProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientDutyPeriodProfile) {
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

    override fun list(): MutableList<LenientDutyPeriodProfile> {
        val out = mutableListOf<LenientDutyPeriodProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientDutyPeriodProfileStore(): LenientDutyPeriodProfileStore {
    return MemoryLenientDutyPeriodProfileStore(
        mutableMapOf<String, LenientDutyPeriodProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientDutyPeriodProfileStore(
    var inner: LenientDutyPeriodProfileStore,
    var cache: TtlCache<LenientDutyPeriodProfile>,
    var hits: Int,
    var misses: Int,
) : LenientDutyPeriodProfileStore {
    override fun find(code: String): LenientDutyPeriodProfile? {
        val hit = (cache.get(code) as LenientDutyPeriodProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientDutyPeriodProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientDutyPeriodProfile> = inner.list()
}

fun newCachedLenientDutyPeriodProfileStore(inner: LenientDutyPeriodProfileStore, ttl: Int, clock: Clock): LenientDutyPeriodProfileStore {
    return CachedLenientDutyPeriodProfileStore(
        inner = inner,
        cache = newTtlCache<LenientDutyPeriodProfile>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient duty period profiles. Empty fields match all. */
data class LenientDutyPeriodProfileQuery(
    var name: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientDutyPeriodProfile): Boolean {
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

    fun apply(items: MutableList<LenientDutyPeriodProfile>): MutableList<LenientDutyPeriodProfile> {
        val matched = filter(items) { candidate: LenientDutyPeriodProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientDutyPeriodProfiles(store: LenientDutyPeriodProfileStore, query: LenientDutyPeriodProfileQuery): MutableList<LenientDutyPeriodProfile> {
    return query.apply(store.list())
}
