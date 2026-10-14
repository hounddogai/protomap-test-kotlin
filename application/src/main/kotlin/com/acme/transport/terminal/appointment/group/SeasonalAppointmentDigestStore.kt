package com.acme.transport.terminal.appointment.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps seasonal appointment digests by code. */
interface SeasonalAppointmentDigestStore {
    fun find(code: String): SeasonalAppointmentDigest?
    fun save(item: SeasonalAppointmentDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalAppointmentDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalAppointmentDigestStore(
    var items: MutableMap<String, SeasonalAppointmentDigest>,
    var order: MutableList<String>,
) : SeasonalAppointmentDigestStore {
    override fun find(code: String): SeasonalAppointmentDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalAppointmentDigest) {
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

    override fun list(): MutableList<SeasonalAppointmentDigest> {
        val out = mutableListOf<SeasonalAppointmentDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalAppointmentDigestStore(): SeasonalAppointmentDigestStore {
    return MemorySeasonalAppointmentDigestStore(
        mutableMapOf<String, SeasonalAppointmentDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalAppointmentDigestStore(
    var inner: SeasonalAppointmentDigestStore,
    var cache: TtlCache<SeasonalAppointmentDigest>,
    var hits: Int,
    var misses: Int,
) : SeasonalAppointmentDigestStore {
    override fun find(code: String): SeasonalAppointmentDigest? {
        val hit = (cache.get(code) as SeasonalAppointmentDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalAppointmentDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalAppointmentDigest> = inner.list()
}

fun newCachedSeasonalAppointmentDigestStore(inner: SeasonalAppointmentDigestStore, ttl: Int, clock: Clock): SeasonalAppointmentDigestStore {
    return CachedSeasonalAppointmentDigestStore(
        inner = inner,
        cache = newTtlCache<SeasonalAppointmentDigest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal appointment digests. Empty fields match all. */
data class SeasonalAppointmentDigestQuery(
    var category: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalAppointmentDigest): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalAppointmentDigest>): MutableList<SeasonalAppointmentDigest> {
        val matched = filter(items) { candidate: SeasonalAppointmentDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalAppointmentDigests(store: SeasonalAppointmentDigestStore, query: SeasonalAppointmentDigestQuery): MutableList<SeasonalAppointmentDigest> {
    return query.apply(store.list())
}
