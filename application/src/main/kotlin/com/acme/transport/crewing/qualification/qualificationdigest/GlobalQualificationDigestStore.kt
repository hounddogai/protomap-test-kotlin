package com.acme.transport.crewing.qualification.qualificationdigest

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global qualification digests by code. */
interface GlobalQualificationDigestStore {
    fun find(code: String): GlobalQualificationDigest?
    fun save(item: GlobalQualificationDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalQualificationDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalQualificationDigestStore(
    var items: MutableMap<String, GlobalQualificationDigest>,
    var order: MutableList<String>,
) : GlobalQualificationDigestStore {
    override fun find(code: String): GlobalQualificationDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalQualificationDigest) {
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

    override fun list(): MutableList<GlobalQualificationDigest> {
        val out = mutableListOf<GlobalQualificationDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalQualificationDigestStore(): GlobalQualificationDigestStore {
    return MemoryGlobalQualificationDigestStore(
        mutableMapOf<String, GlobalQualificationDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalQualificationDigestStore(
    var inner: GlobalQualificationDigestStore,
    var cache: TtlCache<GlobalQualificationDigest>,
    var hits: Int,
    var misses: Int,
) : GlobalQualificationDigestStore {
    override fun find(code: String): GlobalQualificationDigest? {
        val hit = (cache.get(code) as GlobalQualificationDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalQualificationDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalQualificationDigest> = inner.list()
}

fun newCachedGlobalQualificationDigestStore(inner: GlobalQualificationDigestStore, ttl: Int, clock: Clock): GlobalQualificationDigestStore {
    return CachedGlobalQualificationDigestStore(
        inner = inner,
        cache = newTtlCache<GlobalQualificationDigest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global qualification digests. Empty fields match all. */
data class GlobalQualificationDigestQuery(
    var country: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalQualificationDigest): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalQualificationDigest>): MutableList<GlobalQualificationDigest> {
        val matched = filter(items) { candidate: GlobalQualificationDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalQualificationDigests(store: GlobalQualificationDigestStore, query: GlobalQualificationDigestQuery): MutableList<GlobalQualificationDigest> {
    return query.apply(store.list())
}
