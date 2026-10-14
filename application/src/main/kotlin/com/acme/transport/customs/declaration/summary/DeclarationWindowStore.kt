package com.acme.transport.customs.declaration.summary

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration windows by code. */
interface DeclarationWindowStore {
    fun find(code: String): DeclarationWindow?
    fun save(item: DeclarationWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationWindowStore(
    var items: MutableMap<String, DeclarationWindow>,
    var order: MutableList<String>,
) : DeclarationWindowStore {
    override fun find(code: String): DeclarationWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationWindow) {
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

    override fun list(): MutableList<DeclarationWindow> {
        val out = mutableListOf<DeclarationWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationWindowStore(): DeclarationWindowStore {
    return MemoryDeclarationWindowStore(mutableMapOf<String, DeclarationWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationWindowStore(
    var inner: DeclarationWindowStore,
    var cache: TtlCache<DeclarationWindow>,
    var hits: Int,
    var misses: Int,
) : DeclarationWindowStore {
    override fun find(code: String): DeclarationWindow? {
        val hit = (cache.get(code) as DeclarationWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationWindow> = inner.list()
}

fun newCachedDeclarationWindowStore(inner: DeclarationWindowStore, ttl: Int, clock: Clock): DeclarationWindowStore {
    return CachedDeclarationWindowStore(
        inner = inner,
        cache = newTtlCache<DeclarationWindow>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration windows. Empty fields match all. */
data class DeclarationWindowQuery(
    var name: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationWindow): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DeclarationWindow>): MutableList<DeclarationWindow> {
        val matched = filter(items) { candidate: DeclarationWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationWindows(store: DeclarationWindowStore, query: DeclarationWindowQuery): MutableList<DeclarationWindow> {
    return query.apply(store.list())
}
