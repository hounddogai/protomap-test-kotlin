package com.acme.transport.linehaul.backhaul.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul digests by code. */
interface BackhaulDigestStore {
    fun find(code: String): BackhaulDigest?
    fun save(item: BackhaulDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulDigestStore(
    var items: MutableMap<String, BackhaulDigest>,
    var order: MutableList<String>,
) : BackhaulDigestStore {
    override fun find(code: String): BackhaulDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulDigest) {
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

    override fun list(): MutableList<BackhaulDigest> {
        val out = mutableListOf<BackhaulDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulDigestStore(): BackhaulDigestStore {
    return MemoryBackhaulDigestStore(mutableMapOf<String, BackhaulDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulDigestStore(
    var inner: BackhaulDigestStore,
    var cache: TtlCache<BackhaulDigest>,
    var hits: Int,
    var misses: Int,
) : BackhaulDigestStore {
    override fun find(code: String): BackhaulDigest? {
        val hit = (cache.get(code) as BackhaulDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulDigest> = inner.list()
}

fun newCachedBackhaulDigestStore(inner: BackhaulDigestStore, ttl: Int, clock: Clock): BackhaulDigestStore {
    return CachedBackhaulDigestStore(
        inner = inner,
        cache = newTtlCache<BackhaulDigest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul digests. Empty fields match all. */
data class BackhaulDigestQuery(
    var name: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulDigest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<BackhaulDigest>): MutableList<BackhaulDigest> {
        val matched = filter(items) { candidate: BackhaulDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulDigests(store: BackhaulDigestStore, query: BackhaulDigestQuery): MutableList<BackhaulDigest> {
    return query.apply(store.list())
}
