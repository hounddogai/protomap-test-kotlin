package com.acme.transport.customs.declaration.claim

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy declaration claims by code. */
interface LegacyDeclarationClaimStore {
    fun find(code: String): LegacyDeclarationClaim?
    fun save(item: LegacyDeclarationClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyDeclarationClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyDeclarationClaimStore(
    var items: MutableMap<String, LegacyDeclarationClaim>,
    var order: MutableList<String>,
) : LegacyDeclarationClaimStore {
    override fun find(code: String): LegacyDeclarationClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyDeclarationClaim) {
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

    override fun list(): MutableList<LegacyDeclarationClaim> {
        val out = mutableListOf<LegacyDeclarationClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyDeclarationClaimStore(): LegacyDeclarationClaimStore {
    return MemoryLegacyDeclarationClaimStore(
        mutableMapOf<String, LegacyDeclarationClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyDeclarationClaimStore(
    var inner: LegacyDeclarationClaimStore,
    var cache: TtlCache<LegacyDeclarationClaim>,
    var hits: Int,
    var misses: Int,
) : LegacyDeclarationClaimStore {
    override fun find(code: String): LegacyDeclarationClaim? {
        val hit = (cache.get(code) as LegacyDeclarationClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyDeclarationClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyDeclarationClaim> = inner.list()
}

fun newCachedLegacyDeclarationClaimStore(inner: LegacyDeclarationClaimStore, ttl: Int, clock: Clock): LegacyDeclarationClaimStore {
    return CachedLegacyDeclarationClaimStore(
        inner = inner,
        cache = newTtlCache<LegacyDeclarationClaim>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy declaration claims. Empty fields match all. */
data class LegacyDeclarationClaimQuery(
    var name: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyDeclarationClaim): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyDeclarationClaim>): MutableList<LegacyDeclarationClaim> {
        val matched = filter(items) { candidate: LegacyDeclarationClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyDeclarationClaims(store: LegacyDeclarationClaimStore, query: LegacyDeclarationClaimQuery): MutableList<LegacyDeclarationClaim> {
    return query.apply(store.list())
}
