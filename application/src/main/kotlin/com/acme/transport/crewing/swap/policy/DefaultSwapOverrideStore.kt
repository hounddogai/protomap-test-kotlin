package com.acme.transport.crewing.swap.policy

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default swap overrides by code. */
interface DefaultSwapOverrideStore {
    fun find(code: String): DefaultSwapOverride?
    fun save(item: DefaultSwapOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultSwapOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultSwapOverrideStore(
    var items: MutableMap<String, DefaultSwapOverride>,
    var order: MutableList<String>,
) : DefaultSwapOverrideStore {
    override fun find(code: String): DefaultSwapOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultSwapOverride) {
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

    override fun list(): MutableList<DefaultSwapOverride> {
        val out = mutableListOf<DefaultSwapOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultSwapOverrideStore(): DefaultSwapOverrideStore {
    return MemoryDefaultSwapOverrideStore(mutableMapOf<String, DefaultSwapOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultSwapOverrideStore(
    var inner: DefaultSwapOverrideStore,
    var cache: TtlCache<DefaultSwapOverride>,
    var hits: Int,
    var misses: Int,
) : DefaultSwapOverrideStore {
    override fun find(code: String): DefaultSwapOverride? {
        val hit = (cache.get(code) as DefaultSwapOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultSwapOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultSwapOverride> = inner.list()
}

fun newCachedDefaultSwapOverrideStore(inner: DefaultSwapOverrideStore, ttl: Int, clock: Clock): DefaultSwapOverrideStore {
    return CachedDefaultSwapOverrideStore(
        inner = inner,
        cache = newTtlCache<DefaultSwapOverride>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default swap overrides. Empty fields match all. */
data class DefaultSwapOverrideQuery(
    var locale: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultSwapOverride): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultSwapOverride>): MutableList<DefaultSwapOverride> {
        val matched = filter(items) { candidate: DefaultSwapOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultSwapOverrides(store: DefaultSwapOverrideStore, query: DefaultSwapOverrideQuery): MutableList<DefaultSwapOverride> {
    return query.apply(store.list())
}
