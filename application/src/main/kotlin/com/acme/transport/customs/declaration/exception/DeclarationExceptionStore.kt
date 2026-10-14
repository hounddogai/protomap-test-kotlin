package com.acme.transport.customs.declaration.exception

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration exceptions by code. */
interface DeclarationExceptionStore {
    fun find(code: String): DeclarationException?
    fun save(item: DeclarationException)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationExceptionStore(
    var items: MutableMap<String, DeclarationException>,
    var order: MutableList<String>,
) : DeclarationExceptionStore {
    override fun find(code: String): DeclarationException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationException) {
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

    override fun list(): MutableList<DeclarationException> {
        val out = mutableListOf<DeclarationException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationExceptionStore(): DeclarationExceptionStore {
    return MemoryDeclarationExceptionStore(
        mutableMapOf<String, DeclarationException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationExceptionStore(
    var inner: DeclarationExceptionStore,
    var cache: TtlCache<DeclarationException>,
    var hits: Int,
    var misses: Int,
) : DeclarationExceptionStore {
    override fun find(code: String): DeclarationException? {
        val hit = (cache.get(code) as DeclarationException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationException> = inner.list()
}

fun newCachedDeclarationExceptionStore(inner: DeclarationExceptionStore, ttl: Int, clock: Clock): DeclarationExceptionStore {
    return CachedDeclarationExceptionStore(
        inner = inner,
        cache = newTtlCache<DeclarationException>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration exceptions. Empty fields match all. */
data class DeclarationExceptionQuery(
    var name: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationException): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DeclarationException>): MutableList<DeclarationException> {
        val matched = filter(items) { candidate: DeclarationException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationExceptions(store: DeclarationExceptionStore, query: DeclarationExceptionQuery): MutableList<DeclarationException> {
    return query.apply(store.list())
}
