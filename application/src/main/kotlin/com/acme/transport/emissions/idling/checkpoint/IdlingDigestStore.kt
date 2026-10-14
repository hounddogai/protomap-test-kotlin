package com.acme.transport.emissions.idling.checkpoint

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling digests by code. */
interface IdlingDigestStore {
    fun find(code: String): IdlingDigest?
    fun save(item: IdlingDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingDigestStore(
    var items: MutableMap<String, IdlingDigest>,
    var order: MutableList<String>,
) : IdlingDigestStore {
    override fun find(code: String): IdlingDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingDigest) {
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

    override fun list(): MutableList<IdlingDigest> {
        val out = mutableListOf<IdlingDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingDigestStore(): IdlingDigestStore {
    return MemoryIdlingDigestStore(mutableMapOf<String, IdlingDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingDigestStore(
    var inner: IdlingDigestStore,
    var cache: TtlCache<IdlingDigest>,
    var hits: Int,
    var misses: Int,
) : IdlingDigestStore {
    override fun find(code: String): IdlingDigest? {
        val hit = (cache.get(code) as IdlingDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingDigest> = inner.list()
}

fun newCachedIdlingDigestStore(inner: IdlingDigestStore, ttl: Int, clock: Clock): IdlingDigestStore {
    return CachedIdlingDigestStore(
        inner = inner,
        cache = newTtlCache<IdlingDigest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling digests. Empty fields match all. */
data class IdlingDigestQuery(
    var label: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingDigest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IdlingDigest>): MutableList<IdlingDigest> {
        val matched = filter(items) { candidate: IdlingDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingDigests(store: IdlingDigestStore, query: IdlingDigestQuery): MutableList<IdlingDigest> {
    return query.apply(store.list())
}
