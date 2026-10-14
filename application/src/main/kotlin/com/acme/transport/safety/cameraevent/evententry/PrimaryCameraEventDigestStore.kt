package com.acme.transport.safety.cameraevent.evententry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps primary camera event digests by code. */
interface PrimaryCameraEventDigestStore {
    fun find(code: String): PrimaryCameraEventDigest?
    fun save(item: PrimaryCameraEventDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryCameraEventDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryCameraEventDigestStore(
    var items: MutableMap<String, PrimaryCameraEventDigest>,
    var order: MutableList<String>,
) : PrimaryCameraEventDigestStore {
    override fun find(code: String): PrimaryCameraEventDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryCameraEventDigest) {
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

    override fun list(): MutableList<PrimaryCameraEventDigest> {
        val out = mutableListOf<PrimaryCameraEventDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryCameraEventDigestStore(): PrimaryCameraEventDigestStore {
    return MemoryPrimaryCameraEventDigestStore(
        mutableMapOf<String, PrimaryCameraEventDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryCameraEventDigestStore(
    var inner: PrimaryCameraEventDigestStore,
    var cache: TtlCache<PrimaryCameraEventDigest>,
    var hits: Int,
    var misses: Int,
) : PrimaryCameraEventDigestStore {
    override fun find(code: String): PrimaryCameraEventDigest? {
        val hit = (cache.get(code) as PrimaryCameraEventDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryCameraEventDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryCameraEventDigest> = inner.list()
}

fun newCachedPrimaryCameraEventDigestStore(inner: PrimaryCameraEventDigestStore, ttl: Int, clock: Clock): PrimaryCameraEventDigestStore {
    return CachedPrimaryCameraEventDigestStore(
        inner = inner,
        cache = newTtlCache<PrimaryCameraEventDigest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary camera event digests. Empty fields match all. */
data class PrimaryCameraEventDigestQuery(
    var locale: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryCameraEventDigest): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryCameraEventDigest>): MutableList<PrimaryCameraEventDigest> {
        val matched = filter(items) { candidate: PrimaryCameraEventDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryCameraEventDigests(store: PrimaryCameraEventDigestStore, query: PrimaryCameraEventDigestQuery): MutableList<PrimaryCameraEventDigest> {
    return query.apply(store.list())
}
