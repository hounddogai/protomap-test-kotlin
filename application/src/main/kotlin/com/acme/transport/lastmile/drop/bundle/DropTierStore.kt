package com.acme.transport.lastmile.drop.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop tiers by code. */
interface DropTierStore {
    fun find(code: String): DropTier?
    fun save(item: DropTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropTierStore(
    var items: MutableMap<String, DropTier>,
    var order: MutableList<String>,
) : DropTierStore {
    override fun find(code: String): DropTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropTier) {
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

    override fun list(): MutableList<DropTier> {
        val out = mutableListOf<DropTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropTierStore(): DropTierStore {
    return MemoryDropTierStore(mutableMapOf<String, DropTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropTierStore(
    var inner: DropTierStore,
    var cache: TtlCache<DropTier>,
    var hits: Int,
    var misses: Int,
) : DropTierStore {
    override fun find(code: String): DropTier? {
        val hit = (cache.get(code) as DropTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropTier> = inner.list()
}

fun newCachedDropTierStore(inner: DropTierStore, ttl: Int, clock: Clock): DropTierStore {
    return CachedDropTierStore(
        inner = inner,
        cache = newTtlCache<DropTier>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop tiers. Empty fields match all. */
data class DropTierQuery(
    var label: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropTier): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<DropTier>): MutableList<DropTier> {
        val matched = filter(items) { candidate: DropTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropTiers(store: DropTierStore, query: DropTierQuery): MutableList<DropTier> = query.apply(store.list())
