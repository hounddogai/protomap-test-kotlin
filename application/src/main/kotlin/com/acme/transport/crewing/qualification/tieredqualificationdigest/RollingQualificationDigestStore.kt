package com.acme.transport.crewing.qualification.tieredqualificationdigest

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling qualification digests by code. */
interface RollingQualificationDigestStore {
    fun find(code: String): RollingQualificationDigest?
    fun save(item: RollingQualificationDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingQualificationDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingQualificationDigestStore(
    var items: MutableMap<String, RollingQualificationDigest>,
    var order: MutableList<String>,
) : RollingQualificationDigestStore {
    override fun find(code: String): RollingQualificationDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingQualificationDigest) {
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

    override fun list(): MutableList<RollingQualificationDigest> {
        val out = mutableListOf<RollingQualificationDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingQualificationDigestStore(): RollingQualificationDigestStore {
    return MemoryRollingQualificationDigestStore(
        mutableMapOf<String, RollingQualificationDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingQualificationDigestStore(
    var inner: RollingQualificationDigestStore,
    var cache: TtlCache<RollingQualificationDigest>,
    var hits: Int,
    var misses: Int,
) : RollingQualificationDigestStore {
    override fun find(code: String): RollingQualificationDigest? {
        val hit = (cache.get(code) as RollingQualificationDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingQualificationDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingQualificationDigest> = inner.list()
}

fun newCachedRollingQualificationDigestStore(inner: RollingQualificationDigestStore, ttl: Int, clock: Clock): RollingQualificationDigestStore {
    return CachedRollingQualificationDigestStore(
        inner = inner,
        cache = newTtlCache<RollingQualificationDigest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling qualification digests. Empty fields match all. */
data class RollingQualificationDigestQuery(
    var name: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingQualificationDigest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingQualificationDigest>): MutableList<RollingQualificationDigest> {
        val matched = filter(items) { candidate: RollingQualificationDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingQualificationDigests(store: RollingQualificationDigestStore, query: RollingQualificationDigestQuery): MutableList<RollingQualificationDigest> {
    return query.apply(store.list())
}
