package com.acme.transport.terminal.sortation.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps sortation exceptions by code. */
interface SortationExceptionStore {
    fun find(code: String): SortationException?
    fun save(item: SortationException)
    fun remove(code: String): Boolean
    fun list(): MutableList<SortationException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySortationExceptionStore(
    var items: MutableMap<String, SortationException>,
    var order: MutableList<String>,
) : SortationExceptionStore {
    override fun find(code: String): SortationException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SortationException) {
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

    override fun list(): MutableList<SortationException> {
        val out = mutableListOf<SortationException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySortationExceptionStore(): SortationExceptionStore {
    return MemorySortationExceptionStore(mutableMapOf<String, SortationException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSortationExceptionStore(
    var inner: SortationExceptionStore,
    var cache: TtlCache<SortationException>,
    var hits: Int,
    var misses: Int,
) : SortationExceptionStore {
    override fun find(code: String): SortationException? {
        val hit = (cache.get(code) as SortationException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SortationException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SortationException> = inner.list()
}

fun newCachedSortationExceptionStore(inner: SortationExceptionStore, ttl: Int, clock: Clock): SortationExceptionStore {
    return CachedSortationExceptionStore(
        inner = inner,
        cache = newTtlCache<SortationException>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects sortation exceptions. Empty fields match all. */
data class SortationExceptionQuery(
    var lane: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SortationException): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<SortationException>): MutableList<SortationException> {
        val matched = filter(items) { candidate: SortationException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSortationExceptions(store: SortationExceptionStore, query: SortationExceptionQuery): MutableList<SortationException> {
    return query.apply(store.list())
}
