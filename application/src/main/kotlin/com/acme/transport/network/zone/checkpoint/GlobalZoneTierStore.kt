package com.acme.transport.network.zone.checkpoint

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps global zone tiers by code. */
interface GlobalZoneTierStore {
    fun find(code: String): GlobalZoneTier?
    fun save(item: GlobalZoneTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalZoneTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalZoneTierStore(
    var items: MutableMap<String, GlobalZoneTier>,
    var order: MutableList<String>,
) : GlobalZoneTierStore {
    override fun find(code: String): GlobalZoneTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalZoneTier) {
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

    override fun list(): MutableList<GlobalZoneTier> {
        val out = mutableListOf<GlobalZoneTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalZoneTierStore(): GlobalZoneTierStore {
    return MemoryGlobalZoneTierStore(mutableMapOf<String, GlobalZoneTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalZoneTierStore(
    var inner: GlobalZoneTierStore,
    var cache: TtlCache<GlobalZoneTier>,
    var hits: Int,
    var misses: Int,
) : GlobalZoneTierStore {
    override fun find(code: String): GlobalZoneTier? {
        val hit = (cache.get(code) as GlobalZoneTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalZoneTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalZoneTier> = inner.list()
}

fun newCachedGlobalZoneTierStore(inner: GlobalZoneTierStore, ttl: Int, clock: Clock): GlobalZoneTierStore {
    return CachedGlobalZoneTierStore(
        inner = inner,
        cache = newTtlCache<GlobalZoneTier>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global zone tiers. Empty fields match all. */
data class GlobalZoneTierQuery(
    var name: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalZoneTier): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<GlobalZoneTier>): MutableList<GlobalZoneTier> {
        val matched = filter(items) { candidate: GlobalZoneTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalZoneTiers(store: GlobalZoneTierStore, query: GlobalZoneTierQuery): MutableList<GlobalZoneTier> {
    return query.apply(store.list())
}
