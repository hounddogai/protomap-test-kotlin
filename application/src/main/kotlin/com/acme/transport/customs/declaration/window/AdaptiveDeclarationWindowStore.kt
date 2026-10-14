package com.acme.transport.customs.declaration.window

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive declaration windows by code. */
interface AdaptiveDeclarationWindowStore {
    fun find(code: String): AdaptiveDeclarationWindow?
    fun save(item: AdaptiveDeclarationWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveDeclarationWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveDeclarationWindowStore(
    var items: MutableMap<String, AdaptiveDeclarationWindow>,
    var order: MutableList<String>,
) : AdaptiveDeclarationWindowStore {
    override fun find(code: String): AdaptiveDeclarationWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveDeclarationWindow) {
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

    override fun list(): MutableList<AdaptiveDeclarationWindow> {
        val out = mutableListOf<AdaptiveDeclarationWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveDeclarationWindowStore(): AdaptiveDeclarationWindowStore {
    return MemoryAdaptiveDeclarationWindowStore(
        mutableMapOf<String, AdaptiveDeclarationWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveDeclarationWindowStore(
    var inner: AdaptiveDeclarationWindowStore,
    var cache: TtlCache<AdaptiveDeclarationWindow>,
    var hits: Int,
    var misses: Int,
) : AdaptiveDeclarationWindowStore {
    override fun find(code: String): AdaptiveDeclarationWindow? {
        val hit = (cache.get(code) as AdaptiveDeclarationWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveDeclarationWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveDeclarationWindow> = inner.list()
}

fun newCachedAdaptiveDeclarationWindowStore(inner: AdaptiveDeclarationWindowStore, ttl: Int, clock: Clock): AdaptiveDeclarationWindowStore {
    return CachedAdaptiveDeclarationWindowStore(
        inner = inner,
        cache = newTtlCache<AdaptiveDeclarationWindow>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive declaration windows. Empty fields match all. */
data class AdaptiveDeclarationWindowQuery(
    var country: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveDeclarationWindow): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveDeclarationWindow>): MutableList<AdaptiveDeclarationWindow> {
        val matched = filter(items) { candidate: AdaptiveDeclarationWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveDeclarationWindows(store: AdaptiveDeclarationWindowStore, query: AdaptiveDeclarationWindowQuery): MutableList<AdaptiveDeclarationWindow> {
    return query.apply(store.list())
}
