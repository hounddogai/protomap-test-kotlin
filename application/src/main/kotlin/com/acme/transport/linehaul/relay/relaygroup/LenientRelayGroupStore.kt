package com.acme.transport.linehaul.relay.relaygroup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lenient relay groups by code. */
interface LenientRelayGroupStore {
    fun find(code: String): LenientRelayGroup?
    fun save(item: LenientRelayGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientRelayGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientRelayGroupStore(
    var items: MutableMap<String, LenientRelayGroup>,
    var order: MutableList<String>,
) : LenientRelayGroupStore {
    override fun find(code: String): LenientRelayGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientRelayGroup) {
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

    override fun list(): MutableList<LenientRelayGroup> {
        val out = mutableListOf<LenientRelayGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientRelayGroupStore(): LenientRelayGroupStore {
    return MemoryLenientRelayGroupStore(mutableMapOf<String, LenientRelayGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientRelayGroupStore(
    var inner: LenientRelayGroupStore,
    var cache: TtlCache<LenientRelayGroup>,
    var hits: Int,
    var misses: Int,
) : LenientRelayGroupStore {
    override fun find(code: String): LenientRelayGroup? {
        val hit = (cache.get(code) as LenientRelayGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientRelayGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientRelayGroup> = inner.list()
}

fun newCachedLenientRelayGroupStore(inner: LenientRelayGroupStore, ttl: Int, clock: Clock): LenientRelayGroupStore {
    return CachedLenientRelayGroupStore(
        inner = inner,
        cache = newTtlCache<LenientRelayGroup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient relay groups. Empty fields match all. */
data class LenientRelayGroupQuery(
    var title: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientRelayGroup): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<LenientRelayGroup>): MutableList<LenientRelayGroup> {
        val matched = filter(items) { candidate: LenientRelayGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientRelayGroups(store: LenientRelayGroupStore, query: LenientRelayGroupQuery): MutableList<LenientRelayGroup> {
    return query.apply(store.list())
}
