package com.acme.transport.lastmile.attempt.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt digests by code. */
interface AttemptDigestStore {
    fun find(code: String): AttemptDigest?
    fun save(item: AttemptDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptDigestStore(
    var items: MutableMap<String, AttemptDigest>,
    var order: MutableList<String>,
) : AttemptDigestStore {
    override fun find(code: String): AttemptDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptDigest) {
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

    override fun list(): MutableList<AttemptDigest> {
        val out = mutableListOf<AttemptDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptDigestStore(): AttemptDigestStore {
    return MemoryAttemptDigestStore(mutableMapOf<String, AttemptDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptDigestStore(
    var inner: AttemptDigestStore,
    var cache: TtlCache<AttemptDigest>,
    var hits: Int,
    var misses: Int,
) : AttemptDigestStore {
    override fun find(code: String): AttemptDigest? {
        val hit = (cache.get(code) as AttemptDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptDigest> = inner.list()
}

fun newCachedAttemptDigestStore(inner: AttemptDigestStore, ttl: Int, clock: Clock): AttemptDigestStore {
    return CachedAttemptDigestStore(
        inner = inner,
        cache = newTtlCache<AttemptDigest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt digests. Empty fields match all. */
data class AttemptDigestQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptDigest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<AttemptDigest>): MutableList<AttemptDigest> {
        val matched = filter(items) { candidate: AttemptDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptDigests(store: AttemptDigestStore, query: AttemptDigestQuery): MutableList<AttemptDigest> {
    return query.apply(store.list())
}
