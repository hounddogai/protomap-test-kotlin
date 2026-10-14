package com.acme.transport.customs.declaration.policy

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary declaration overrides by code. */
interface PrimaryDeclarationOverrideStore {
    fun find(code: String): PrimaryDeclarationOverride?
    fun save(item: PrimaryDeclarationOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryDeclarationOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryDeclarationOverrideStore(
    var items: MutableMap<String, PrimaryDeclarationOverride>,
    var order: MutableList<String>,
) : PrimaryDeclarationOverrideStore {
    override fun find(code: String): PrimaryDeclarationOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryDeclarationOverride) {
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

    override fun list(): MutableList<PrimaryDeclarationOverride> {
        val out = mutableListOf<PrimaryDeclarationOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryDeclarationOverrideStore(): PrimaryDeclarationOverrideStore {
    return MemoryPrimaryDeclarationOverrideStore(
        mutableMapOf<String, PrimaryDeclarationOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryDeclarationOverrideStore(
    var inner: PrimaryDeclarationOverrideStore,
    var cache: TtlCache<PrimaryDeclarationOverride>,
    var hits: Int,
    var misses: Int,
) : PrimaryDeclarationOverrideStore {
    override fun find(code: String): PrimaryDeclarationOverride? {
        val hit = (cache.get(code) as PrimaryDeclarationOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryDeclarationOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryDeclarationOverride> = inner.list()
}

fun newCachedPrimaryDeclarationOverrideStore(inner: PrimaryDeclarationOverrideStore, ttl: Int, clock: Clock): PrimaryDeclarationOverrideStore {
    return CachedPrimaryDeclarationOverrideStore(
        inner = inner,
        cache = newTtlCache<PrimaryDeclarationOverride>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary declaration overrides. Empty fields match all. */
data class PrimaryDeclarationOverrideQuery(
    var segment: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryDeclarationOverride): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<PrimaryDeclarationOverride>): MutableList<PrimaryDeclarationOverride> {
        val matched = filter(items) { candidate: PrimaryDeclarationOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryDeclarationOverrides(store: PrimaryDeclarationOverrideStore, query: PrimaryDeclarationOverrideQuery): MutableList<PrimaryDeclarationOverride> {
    return query.apply(store.list())
}
