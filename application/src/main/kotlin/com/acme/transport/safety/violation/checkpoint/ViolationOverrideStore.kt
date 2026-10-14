package com.acme.transport.safety.violation.checkpoint

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation overrides by code. */
interface ViolationOverrideStore {
    fun find(code: String): ViolationOverride?
    fun save(item: ViolationOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationOverrideStore(
    var items: MutableMap<String, ViolationOverride>,
    var order: MutableList<String>,
) : ViolationOverrideStore {
    override fun find(code: String): ViolationOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationOverride) {
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

    override fun list(): MutableList<ViolationOverride> {
        val out = mutableListOf<ViolationOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationOverrideStore(): ViolationOverrideStore {
    return MemoryViolationOverrideStore(mutableMapOf<String, ViolationOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationOverrideStore(
    var inner: ViolationOverrideStore,
    var cache: TtlCache<ViolationOverride>,
    var hits: Int,
    var misses: Int,
) : ViolationOverrideStore {
    override fun find(code: String): ViolationOverride? {
        val hit = (cache.get(code) as ViolationOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationOverride> = inner.list()
}

fun newCachedViolationOverrideStore(inner: ViolationOverrideStore, ttl: Int, clock: Clock): ViolationOverrideStore {
    return CachedViolationOverrideStore(
        inner = inner,
        cache = newTtlCache<ViolationOverride>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation overrides. Empty fields match all. */
data class ViolationOverrideQuery(
    var name: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationOverride): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<ViolationOverride>): MutableList<ViolationOverride> {
        val matched = filter(items) { candidate: ViolationOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationOverrides(store: ViolationOverrideStore, query: ViolationOverrideQuery): MutableList<ViolationOverride> {
    return query.apply(store.list())
}
