package com.acme.transport.terminal.crossdock.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock digests by code. */
interface CrossDockDigestStore {
    fun find(code: String): CrossDockDigest?
    fun save(item: CrossDockDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockDigestStore(
    var items: MutableMap<String, CrossDockDigest>,
    var order: MutableList<String>,
) : CrossDockDigestStore {
    override fun find(code: String): CrossDockDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockDigest) {
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

    override fun list(): MutableList<CrossDockDigest> {
        val out = mutableListOf<CrossDockDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockDigestStore(): CrossDockDigestStore {
    return MemoryCrossDockDigestStore(mutableMapOf<String, CrossDockDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockDigestStore(
    var inner: CrossDockDigestStore,
    var cache: TtlCache<CrossDockDigest>,
    var hits: Int,
    var misses: Int,
) : CrossDockDigestStore {
    override fun find(code: String): CrossDockDigest? {
        val hit = (cache.get(code) as CrossDockDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockDigest> = inner.list()
}

fun newCachedCrossDockDigestStore(inner: CrossDockDigestStore, ttl: Int, clock: Clock): CrossDockDigestStore {
    return CachedCrossDockDigestStore(
        inner = inner,
        cache = newTtlCache<CrossDockDigest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock digests. Empty fields match all. */
data class CrossDockDigestQuery(
    var title: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockDigest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CrossDockDigest>): MutableList<CrossDockDigest> {
        val matched = filter(items) { candidate: CrossDockDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockDigests(store: CrossDockDigestStore, query: CrossDockDigestQuery): MutableList<CrossDockDigest> {
    return query.apply(store.list())
}
