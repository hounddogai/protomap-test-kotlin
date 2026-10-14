package com.acme.transport.linehaul.backhaul.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps default backhaul digests by code. */
interface DefaultBackhaulDigestStore {
    fun find(code: String): DefaultBackhaulDigest?
    fun save(item: DefaultBackhaulDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultBackhaulDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultBackhaulDigestStore(
    var items: MutableMap<String, DefaultBackhaulDigest>,
    var order: MutableList<String>,
) : DefaultBackhaulDigestStore {
    override fun find(code: String): DefaultBackhaulDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultBackhaulDigest) {
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

    override fun list(): MutableList<DefaultBackhaulDigest> {
        val out = mutableListOf<DefaultBackhaulDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultBackhaulDigestStore(): DefaultBackhaulDigestStore {
    return MemoryDefaultBackhaulDigestStore(
        mutableMapOf<String, DefaultBackhaulDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultBackhaulDigestStore(
    var inner: DefaultBackhaulDigestStore,
    var cache: TtlCache<DefaultBackhaulDigest>,
    var hits: Int,
    var misses: Int,
) : DefaultBackhaulDigestStore {
    override fun find(code: String): DefaultBackhaulDigest? {
        val hit = (cache.get(code) as DefaultBackhaulDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultBackhaulDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultBackhaulDigest> = inner.list()
}

fun newCachedDefaultBackhaulDigestStore(inner: DefaultBackhaulDigestStore, ttl: Int, clock: Clock): DefaultBackhaulDigestStore {
    return CachedDefaultBackhaulDigestStore(
        inner = inner,
        cache = newTtlCache<DefaultBackhaulDigest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default backhaul digests. Empty fields match all. */
data class DefaultBackhaulDigestQuery(
    var name: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultBackhaulDigest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultBackhaulDigest>): MutableList<DefaultBackhaulDigest> {
        val matched = filter(items) { candidate: DefaultBackhaulDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultBackhaulDigests(store: DefaultBackhaulDigestStore, query: DefaultBackhaulDigestQuery): MutableList<DefaultBackhaulDigest> {
    return query.apply(store.list())
}
