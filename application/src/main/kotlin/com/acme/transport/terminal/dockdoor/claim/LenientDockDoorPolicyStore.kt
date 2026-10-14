package com.acme.transport.terminal.dockdoor.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps lenient dock door policies by code. */
interface LenientDockDoorPolicyStore {
    fun find(code: String): LenientDockDoorPolicy?
    fun save(item: LenientDockDoorPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientDockDoorPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientDockDoorPolicyStore(
    var items: MutableMap<String, LenientDockDoorPolicy>,
    var order: MutableList<String>,
) : LenientDockDoorPolicyStore {
    override fun find(code: String): LenientDockDoorPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientDockDoorPolicy) {
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

    override fun list(): MutableList<LenientDockDoorPolicy> {
        val out = mutableListOf<LenientDockDoorPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientDockDoorPolicyStore(): LenientDockDoorPolicyStore {
    return MemoryLenientDockDoorPolicyStore(
        mutableMapOf<String, LenientDockDoorPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientDockDoorPolicyStore(
    var inner: LenientDockDoorPolicyStore,
    var cache: TtlCache<LenientDockDoorPolicy>,
    var hits: Int,
    var misses: Int,
) : LenientDockDoorPolicyStore {
    override fun find(code: String): LenientDockDoorPolicy? {
        val hit = (cache.get(code) as LenientDockDoorPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientDockDoorPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientDockDoorPolicy> = inner.list()
}

fun newCachedLenientDockDoorPolicyStore(inner: LenientDockDoorPolicyStore, ttl: Int, clock: Clock): LenientDockDoorPolicyStore {
    return CachedLenientDockDoorPolicyStore(
        inner = inner,
        cache = newTtlCache<LenientDockDoorPolicy>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient dock door policies. Empty fields match all. */
data class LenientDockDoorPolicyQuery(
    var title: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientDockDoorPolicy): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientDockDoorPolicy>): MutableList<LenientDockDoorPolicy> {
        val matched = filter(items) { candidate: LenientDockDoorPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientDockDoorPolicies(store: LenientDockDoorPolicyStore, query: LenientDockDoorPolicyQuery): MutableList<LenientDockDoorPolicy> {
    return query.apply(store.list())
}
