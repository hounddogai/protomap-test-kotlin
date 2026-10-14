package com.acme.transport.terminal.sortation.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps sortation requests by code. */
interface SortationRequestStore {
    fun find(code: String): SortationRequest?
    fun save(item: SortationRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<SortationRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySortationRequestStore(
    var items: MutableMap<String, SortationRequest>,
    var order: MutableList<String>,
) : SortationRequestStore {
    override fun find(code: String): SortationRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SortationRequest) {
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

    override fun list(): MutableList<SortationRequest> {
        val out = mutableListOf<SortationRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySortationRequestStore(): SortationRequestStore {
    return MemorySortationRequestStore(mutableMapOf<String, SortationRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSortationRequestStore(
    var inner: SortationRequestStore,
    var cache: TtlCache<SortationRequest>,
    var hits: Int,
    var misses: Int,
) : SortationRequestStore {
    override fun find(code: String): SortationRequest? {
        val hit = (cache.get(code) as SortationRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SortationRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SortationRequest> = inner.list()
}

fun newCachedSortationRequestStore(inner: SortationRequestStore, ttl: Int, clock: Clock): SortationRequestStore {
    return CachedSortationRequestStore(
        inner = inner,
        cache = newTtlCache<SortationRequest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects sortation requests. Empty fields match all. */
data class SortationRequestQuery(
    var country: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SortationRequest): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SortationRequest>): MutableList<SortationRequest> {
        val matched = filter(items) { candidate: SortationRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSortationRequests(store: SortationRequestStore, query: SortationRequestQuery): MutableList<SortationRequest> {
    return query.apply(store.list())
}
