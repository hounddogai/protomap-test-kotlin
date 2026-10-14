package com.acme.transport.terminal.crossdock.strictdockslot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps tiered cross dock profiles by code. */
interface TieredCrossDockProfileStore {
    fun find(code: String): TieredCrossDockProfile?
    fun save(item: TieredCrossDockProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredCrossDockProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredCrossDockProfileStore(
    var items: MutableMap<String, TieredCrossDockProfile>,
    var order: MutableList<String>,
) : TieredCrossDockProfileStore {
    override fun find(code: String): TieredCrossDockProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredCrossDockProfile) {
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

    override fun list(): MutableList<TieredCrossDockProfile> {
        val out = mutableListOf<TieredCrossDockProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredCrossDockProfileStore(): TieredCrossDockProfileStore {
    return MemoryTieredCrossDockProfileStore(
        mutableMapOf<String, TieredCrossDockProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredCrossDockProfileStore(
    var inner: TieredCrossDockProfileStore,
    var cache: TtlCache<TieredCrossDockProfile>,
    var hits: Int,
    var misses: Int,
) : TieredCrossDockProfileStore {
    override fun find(code: String): TieredCrossDockProfile? {
        val hit = (cache.get(code) as TieredCrossDockProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredCrossDockProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredCrossDockProfile> = inner.list()
}

fun newCachedTieredCrossDockProfileStore(inner: TieredCrossDockProfileStore, ttl: Int, clock: Clock): TieredCrossDockProfileStore {
    return CachedTieredCrossDockProfileStore(
        inner = inner,
        cache = newTtlCache<TieredCrossDockProfile>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered cross dock profiles. Empty fields match all. */
data class TieredCrossDockProfileQuery(
    var channel: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredCrossDockProfile): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredCrossDockProfile>): MutableList<TieredCrossDockProfile> {
        val matched = filter(items) { candidate: TieredCrossDockProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredCrossDockProfiles(store: TieredCrossDockProfileStore, query: TieredCrossDockProfileQuery): MutableList<TieredCrossDockProfile> {
    return query.apply(store.list())
}
