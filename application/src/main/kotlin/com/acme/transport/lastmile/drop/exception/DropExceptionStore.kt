package com.acme.transport.lastmile.drop.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop exceptions by code. */
interface DropExceptionStore {
    fun find(code: String): DropException?
    fun save(item: DropException)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropExceptionStore(
    var items: MutableMap<String, DropException>,
    var order: MutableList<String>,
) : DropExceptionStore {
    override fun find(code: String): DropException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropException) {
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

    override fun list(): MutableList<DropException> {
        val out = mutableListOf<DropException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropExceptionStore(): DropExceptionStore {
    return MemoryDropExceptionStore(mutableMapOf<String, DropException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropExceptionStore(
    var inner: DropExceptionStore,
    var cache: TtlCache<DropException>,
    var hits: Int,
    var misses: Int,
) : DropExceptionStore {
    override fun find(code: String): DropException? {
        val hit = (cache.get(code) as DropException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropException> = inner.list()
}

fun newCachedDropExceptionStore(inner: DropExceptionStore, ttl: Int, clock: Clock): DropExceptionStore {
    return CachedDropExceptionStore(
        inner = inner,
        cache = newTtlCache<DropException>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop exceptions. Empty fields match all. */
data class DropExceptionQuery(
    var source: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropException): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DropException>): MutableList<DropException> {
        val matched = filter(items) { candidate: DropException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropExceptions(store: DropExceptionStore, query: DropExceptionQuery): MutableList<DropException> {
    return query.apply(store.list())
}
