package com.acme.transport.safety.scorecard.scorecardprofile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps rolling scorecard profiles by code. */
interface RollingScorecardProfileStore {
    fun find(code: String): RollingScorecardProfile?
    fun save(item: RollingScorecardProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingScorecardProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingScorecardProfileStore(
    var items: MutableMap<String, RollingScorecardProfile>,
    var order: MutableList<String>,
) : RollingScorecardProfileStore {
    override fun find(code: String): RollingScorecardProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingScorecardProfile) {
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

    override fun list(): MutableList<RollingScorecardProfile> {
        val out = mutableListOf<RollingScorecardProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingScorecardProfileStore(): RollingScorecardProfileStore {
    return MemoryRollingScorecardProfileStore(
        mutableMapOf<String, RollingScorecardProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingScorecardProfileStore(
    var inner: RollingScorecardProfileStore,
    var cache: TtlCache<RollingScorecardProfile>,
    var hits: Int,
    var misses: Int,
) : RollingScorecardProfileStore {
    override fun find(code: String): RollingScorecardProfile? {
        val hit = (cache.get(code) as RollingScorecardProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingScorecardProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingScorecardProfile> = inner.list()
}

fun newCachedRollingScorecardProfileStore(inner: RollingScorecardProfileStore, ttl: Int, clock: Clock): RollingScorecardProfileStore {
    return CachedRollingScorecardProfileStore(
        inner = inner,
        cache = newTtlCache<RollingScorecardProfile>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling scorecard profiles. Empty fields match all. */
data class RollingScorecardProfileQuery(
    var locale: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingScorecardProfile): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingScorecardProfile>): MutableList<RollingScorecardProfile> {
        val matched = filter(items) { candidate: RollingScorecardProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingScorecardProfiles(store: RollingScorecardProfileStore, query: RollingScorecardProfileQuery): MutableList<RollingScorecardProfile> {
    return query.apply(store.list())
}
