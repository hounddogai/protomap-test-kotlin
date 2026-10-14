package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard digests by code. */
interface ScorecardDigestStore {
    fun find(code: String): ScorecardDigest?
    fun save(item: ScorecardDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardDigestStore(
    var items: MutableMap<String, ScorecardDigest>,
    var order: MutableList<String>,
) : ScorecardDigestStore {
    override fun find(code: String): ScorecardDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardDigest) {
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

    override fun list(): MutableList<ScorecardDigest> {
        val out = mutableListOf<ScorecardDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardDigestStore(): ScorecardDigestStore {
    return MemoryScorecardDigestStore(mutableMapOf<String, ScorecardDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardDigestStore(
    var inner: ScorecardDigestStore,
    var cache: TtlCache<ScorecardDigest>,
    var hits: Int,
    var misses: Int,
) : ScorecardDigestStore {
    override fun find(code: String): ScorecardDigest? {
        val hit = (cache.get(code) as ScorecardDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardDigest> = inner.list()
}

fun newCachedScorecardDigestStore(inner: ScorecardDigestStore, ttl: Int, clock: Clock): ScorecardDigestStore {
    return CachedScorecardDigestStore(
        inner = inner,
        cache = newTtlCache<ScorecardDigest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard digests. Empty fields match all. */
data class ScorecardDigestQuery(
    var label: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardDigest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScorecardDigest>): MutableList<ScorecardDigest> {
        val matched = filter(items) { candidate: ScorecardDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardDigests(store: ScorecardDigestStore, query: ScorecardDigestQuery): MutableList<ScorecardDigest> {
    return query.apply(store.list())
}
