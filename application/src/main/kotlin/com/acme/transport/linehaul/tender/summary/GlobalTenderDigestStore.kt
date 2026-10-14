package com.acme.transport.linehaul.tender.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps global tender digests by code. */
interface GlobalTenderDigestStore {
    fun find(code: String): GlobalTenderDigest?
    fun save(item: GlobalTenderDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalTenderDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalTenderDigestStore(
    var items: MutableMap<String, GlobalTenderDigest>,
    var order: MutableList<String>,
) : GlobalTenderDigestStore {
    override fun find(code: String): GlobalTenderDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalTenderDigest) {
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

    override fun list(): MutableList<GlobalTenderDigest> {
        val out = mutableListOf<GlobalTenderDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalTenderDigestStore(): GlobalTenderDigestStore {
    return MemoryGlobalTenderDigestStore(mutableMapOf<String, GlobalTenderDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalTenderDigestStore(
    var inner: GlobalTenderDigestStore,
    var cache: TtlCache<GlobalTenderDigest>,
    var hits: Int,
    var misses: Int,
) : GlobalTenderDigestStore {
    override fun find(code: String): GlobalTenderDigest? {
        val hit = (cache.get(code) as GlobalTenderDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalTenderDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalTenderDigest> = inner.list()
}

fun newCachedGlobalTenderDigestStore(inner: GlobalTenderDigestStore, ttl: Int, clock: Clock): GlobalTenderDigestStore {
    return CachedGlobalTenderDigestStore(
        inner = inner,
        cache = newTtlCache<GlobalTenderDigest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global tender digests. Empty fields match all. */
data class GlobalTenderDigestQuery(
    var category: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalTenderDigest): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<GlobalTenderDigest>): MutableList<GlobalTenderDigest> {
        val matched = filter(items) { candidate: GlobalTenderDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalTenderDigests(store: GlobalTenderDigestStore, query: GlobalTenderDigestQuery): MutableList<GlobalTenderDigest> {
    return query.apply(store.list())
}
