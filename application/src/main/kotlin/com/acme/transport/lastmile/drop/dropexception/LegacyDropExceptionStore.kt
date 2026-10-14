package com.acme.transport.lastmile.drop.dropexception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps legacy drop exceptions by code. */
interface LegacyDropExceptionStore {
    fun find(code: String): LegacyDropException?
    fun save(item: LegacyDropException)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyDropException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyDropExceptionStore(
    var items: MutableMap<String, LegacyDropException>,
    var order: MutableList<String>,
) : LegacyDropExceptionStore {
    override fun find(code: String): LegacyDropException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyDropException) {
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

    override fun list(): MutableList<LegacyDropException> {
        val out = mutableListOf<LegacyDropException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyDropExceptionStore(): LegacyDropExceptionStore {
    return MemoryLegacyDropExceptionStore(mutableMapOf<String, LegacyDropException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyDropExceptionStore(
    var inner: LegacyDropExceptionStore,
    var cache: TtlCache<LegacyDropException>,
    var hits: Int,
    var misses: Int,
) : LegacyDropExceptionStore {
    override fun find(code: String): LegacyDropException? {
        val hit = (cache.get(code) as LegacyDropException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyDropException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyDropException> = inner.list()
}

fun newCachedLegacyDropExceptionStore(inner: LegacyDropExceptionStore, ttl: Int, clock: Clock): LegacyDropExceptionStore {
    return CachedLegacyDropExceptionStore(
        inner = inner,
        cache = newTtlCache<LegacyDropException>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy drop exceptions. Empty fields match all. */
data class LegacyDropExceptionQuery(
    var label: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyDropException): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyDropException>): MutableList<LegacyDropException> {
        val matched = filter(items) { candidate: LegacyDropException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyDropExceptions(store: LegacyDropExceptionStore, query: LegacyDropExceptionQuery): MutableList<LegacyDropException> {
    return query.apply(store.list())
}
