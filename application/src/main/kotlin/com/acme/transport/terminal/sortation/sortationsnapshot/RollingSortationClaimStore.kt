package com.acme.transport.terminal.sortation.sortationsnapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps rolling sortation claims by code. */
interface RollingSortationClaimStore {
    fun find(code: String): RollingSortationClaim?
    fun save(item: RollingSortationClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingSortationClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingSortationClaimStore(
    var items: MutableMap<String, RollingSortationClaim>,
    var order: MutableList<String>,
) : RollingSortationClaimStore {
    override fun find(code: String): RollingSortationClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingSortationClaim) {
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

    override fun list(): MutableList<RollingSortationClaim> {
        val out = mutableListOf<RollingSortationClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingSortationClaimStore(): RollingSortationClaimStore {
    return MemoryRollingSortationClaimStore(
        mutableMapOf<String, RollingSortationClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingSortationClaimStore(
    var inner: RollingSortationClaimStore,
    var cache: TtlCache<RollingSortationClaim>,
    var hits: Int,
    var misses: Int,
) : RollingSortationClaimStore {
    override fun find(code: String): RollingSortationClaim? {
        val hit = (cache.get(code) as RollingSortationClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingSortationClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingSortationClaim> = inner.list()
}

fun newCachedRollingSortationClaimStore(inner: RollingSortationClaimStore, ttl: Int, clock: Clock): RollingSortationClaimStore {
    return CachedRollingSortationClaimStore(
        inner = inner,
        cache = newTtlCache<RollingSortationClaim>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling sortation claims. Empty fields match all. */
data class RollingSortationClaimQuery(
    var label: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingSortationClaim): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingSortationClaim>): MutableList<RollingSortationClaim> {
        val matched = filter(items) { candidate: RollingSortationClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingSortationClaims(store: RollingSortationClaimStore, query: RollingSortationClaimQuery): MutableList<RollingSortationClaim> {
    return query.apply(store.list())
}
