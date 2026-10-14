package com.acme.transport.customs.declaration.declarationwindow

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered declaration windows by code. */
interface TieredDeclarationWindowStore {
    fun find(code: String): TieredDeclarationWindow?
    fun save(item: TieredDeclarationWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredDeclarationWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredDeclarationWindowStore(
    var items: MutableMap<String, TieredDeclarationWindow>,
    var order: MutableList<String>,
) : TieredDeclarationWindowStore {
    override fun find(code: String): TieredDeclarationWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredDeclarationWindow) {
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

    override fun list(): MutableList<TieredDeclarationWindow> {
        val out = mutableListOf<TieredDeclarationWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredDeclarationWindowStore(): TieredDeclarationWindowStore {
    return MemoryTieredDeclarationWindowStore(
        mutableMapOf<String, TieredDeclarationWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredDeclarationWindowStore(
    var inner: TieredDeclarationWindowStore,
    var cache: TtlCache<TieredDeclarationWindow>,
    var hits: Int,
    var misses: Int,
) : TieredDeclarationWindowStore {
    override fun find(code: String): TieredDeclarationWindow? {
        val hit = (cache.get(code) as TieredDeclarationWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredDeclarationWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredDeclarationWindow> = inner.list()
}

fun newCachedTieredDeclarationWindowStore(inner: TieredDeclarationWindowStore, ttl: Int, clock: Clock): TieredDeclarationWindowStore {
    return CachedTieredDeclarationWindowStore(
        inner = inner,
        cache = newTtlCache<TieredDeclarationWindow>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered declaration windows. Empty fields match all. */
data class TieredDeclarationWindowQuery(
    var title: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredDeclarationWindow): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredDeclarationWindow>): MutableList<TieredDeclarationWindow> {
        val matched = filter(items) { candidate: TieredDeclarationWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredDeclarationWindows(store: TieredDeclarationWindowStore, query: TieredDeclarationWindowQuery): MutableList<TieredDeclarationWindow> {
    return query.apply(store.list())
}
