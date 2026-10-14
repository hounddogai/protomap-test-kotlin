package com.acme.transport.linehaul.tender.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps rolling tender groups by code. */
interface RollingTenderGroupStore {
    fun find(code: String): RollingTenderGroup?
    fun save(item: RollingTenderGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingTenderGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingTenderGroupStore(
    var items: MutableMap<String, RollingTenderGroup>,
    var order: MutableList<String>,
) : RollingTenderGroupStore {
    override fun find(code: String): RollingTenderGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingTenderGroup) {
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

    override fun list(): MutableList<RollingTenderGroup> {
        val out = mutableListOf<RollingTenderGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingTenderGroupStore(): RollingTenderGroupStore {
    return MemoryRollingTenderGroupStore(mutableMapOf<String, RollingTenderGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingTenderGroupStore(
    var inner: RollingTenderGroupStore,
    var cache: TtlCache<RollingTenderGroup>,
    var hits: Int,
    var misses: Int,
) : RollingTenderGroupStore {
    override fun find(code: String): RollingTenderGroup? {
        val hit = (cache.get(code) as RollingTenderGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingTenderGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingTenderGroup> = inner.list()
}

fun newCachedRollingTenderGroupStore(inner: RollingTenderGroupStore, ttl: Int, clock: Clock): RollingTenderGroupStore {
    return CachedRollingTenderGroupStore(
        inner = inner,
        cache = newTtlCache<RollingTenderGroup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling tender groups. Empty fields match all. */
data class RollingTenderGroupQuery(
    var name: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingTenderGroup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingTenderGroup>): MutableList<RollingTenderGroup> {
        val matched = filter(items) { candidate: RollingTenderGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingTenderGroups(store: RollingTenderGroupStore, query: RollingTenderGroupQuery): MutableList<RollingTenderGroup> {
    return query.apply(store.list())
}
