package com.acme.transport.terminal.crossdock.dockslot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps legacy cross dock exceptions by code. */
interface LegacyCrossDockExceptionStore {
    fun find(code: String): LegacyCrossDockException?
    fun save(item: LegacyCrossDockException)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyCrossDockException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyCrossDockExceptionStore(
    var items: MutableMap<String, LegacyCrossDockException>,
    var order: MutableList<String>,
) : LegacyCrossDockExceptionStore {
    override fun find(code: String): LegacyCrossDockException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyCrossDockException) {
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

    override fun list(): MutableList<LegacyCrossDockException> {
        val out = mutableListOf<LegacyCrossDockException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyCrossDockExceptionStore(): LegacyCrossDockExceptionStore {
    return MemoryLegacyCrossDockExceptionStore(
        mutableMapOf<String, LegacyCrossDockException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyCrossDockExceptionStore(
    var inner: LegacyCrossDockExceptionStore,
    var cache: TtlCache<LegacyCrossDockException>,
    var hits: Int,
    var misses: Int,
) : LegacyCrossDockExceptionStore {
    override fun find(code: String): LegacyCrossDockException? {
        val hit = (cache.get(code) as LegacyCrossDockException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyCrossDockException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyCrossDockException> = inner.list()
}

fun newCachedLegacyCrossDockExceptionStore(inner: LegacyCrossDockExceptionStore, ttl: Int, clock: Clock): LegacyCrossDockExceptionStore {
    return CachedLegacyCrossDockExceptionStore(
        inner = inner,
        cache = newTtlCache<LegacyCrossDockException>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy cross dock exceptions. Empty fields match all. */
data class LegacyCrossDockExceptionQuery(
    var locale: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyCrossDockException): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyCrossDockException>): MutableList<LegacyCrossDockException> {
        val matched = filter(items) { candidate: LegacyCrossDockException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyCrossDockExceptions(store: LegacyCrossDockExceptionStore, query: LegacyCrossDockExceptionQuery): MutableList<LegacyCrossDockException> {
    return query.apply(store.list())
}
