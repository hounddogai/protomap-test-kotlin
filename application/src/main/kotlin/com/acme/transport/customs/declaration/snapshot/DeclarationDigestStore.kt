package com.acme.transport.customs.declaration.snapshot

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration digests by code. */
interface DeclarationDigestStore {
    fun find(code: String): DeclarationDigest?
    fun save(item: DeclarationDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationDigestStore(
    var items: MutableMap<String, DeclarationDigest>,
    var order: MutableList<String>,
) : DeclarationDigestStore {
    override fun find(code: String): DeclarationDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationDigest) {
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

    override fun list(): MutableList<DeclarationDigest> {
        val out = mutableListOf<DeclarationDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationDigestStore(): DeclarationDigestStore {
    return MemoryDeclarationDigestStore(mutableMapOf<String, DeclarationDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationDigestStore(
    var inner: DeclarationDigestStore,
    var cache: TtlCache<DeclarationDigest>,
    var hits: Int,
    var misses: Int,
) : DeclarationDigestStore {
    override fun find(code: String): DeclarationDigest? {
        val hit = (cache.get(code) as DeclarationDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationDigest> = inner.list()
}

fun newCachedDeclarationDigestStore(inner: DeclarationDigestStore, ttl: Int, clock: Clock): DeclarationDigestStore {
    return CachedDeclarationDigestStore(
        inner = inner,
        cache = newTtlCache<DeclarationDigest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration digests. Empty fields match all. */
data class DeclarationDigestQuery(
    var name: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationDigest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DeclarationDigest>): MutableList<DeclarationDigest> {
        val matched = filter(items) { candidate: DeclarationDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationDigests(store: DeclarationDigestStore, query: DeclarationDigestQuery): MutableList<DeclarationDigest> {
    return query.apply(store.list())
}
