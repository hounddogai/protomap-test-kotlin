package com.acme.transport.customs.declaration.digest

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary declaration digests by code. */
interface PrimaryDeclarationDigestStore {
    fun find(code: String): PrimaryDeclarationDigest?
    fun save(item: PrimaryDeclarationDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryDeclarationDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryDeclarationDigestStore(
    var items: MutableMap<String, PrimaryDeclarationDigest>,
    var order: MutableList<String>,
) : PrimaryDeclarationDigestStore {
    override fun find(code: String): PrimaryDeclarationDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryDeclarationDigest) {
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

    override fun list(): MutableList<PrimaryDeclarationDigest> {
        val out = mutableListOf<PrimaryDeclarationDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryDeclarationDigestStore(): PrimaryDeclarationDigestStore {
    return MemoryPrimaryDeclarationDigestStore(
        mutableMapOf<String, PrimaryDeclarationDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryDeclarationDigestStore(
    var inner: PrimaryDeclarationDigestStore,
    var cache: TtlCache<PrimaryDeclarationDigest>,
    var hits: Int,
    var misses: Int,
) : PrimaryDeclarationDigestStore {
    override fun find(code: String): PrimaryDeclarationDigest? {
        val hit = (cache.get(code) as PrimaryDeclarationDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryDeclarationDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryDeclarationDigest> = inner.list()
}

fun newCachedPrimaryDeclarationDigestStore(inner: PrimaryDeclarationDigestStore, ttl: Int, clock: Clock): PrimaryDeclarationDigestStore {
    return CachedPrimaryDeclarationDigestStore(
        inner = inner,
        cache = newTtlCache<PrimaryDeclarationDigest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary declaration digests. Empty fields match all. */
data class PrimaryDeclarationDigestQuery(
    var title: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryDeclarationDigest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryDeclarationDigest>): MutableList<PrimaryDeclarationDigest> {
        val matched = filter(items) { candidate: PrimaryDeclarationDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryDeclarationDigests(store: PrimaryDeclarationDigestStore, query: PrimaryDeclarationDigestQuery): MutableList<PrimaryDeclarationDigest> {
    return query.apply(store.list())
}
