package com.acme.transport.emissions.idling.notice

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling requests by code. */
interface IdlingRequestStore {
    fun find(code: String): IdlingRequest?
    fun save(item: IdlingRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingRequestStore(
    var items: MutableMap<String, IdlingRequest>,
    var order: MutableList<String>,
) : IdlingRequestStore {
    override fun find(code: String): IdlingRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingRequest) {
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

    override fun list(): MutableList<IdlingRequest> {
        val out = mutableListOf<IdlingRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingRequestStore(): IdlingRequestStore {
    return MemoryIdlingRequestStore(mutableMapOf<String, IdlingRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingRequestStore(
    var inner: IdlingRequestStore,
    var cache: TtlCache<IdlingRequest>,
    var hits: Int,
    var misses: Int,
) : IdlingRequestStore {
    override fun find(code: String): IdlingRequest? {
        val hit = (cache.get(code) as IdlingRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingRequest> = inner.list()
}

fun newCachedIdlingRequestStore(inner: IdlingRequestStore, ttl: Int, clock: Clock): IdlingRequestStore {
    return CachedIdlingRequestStore(
        inner = inner,
        cache = newTtlCache<IdlingRequest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling requests. Empty fields match all. */
data class IdlingRequestQuery(
    var label: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingRequest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IdlingRequest>): MutableList<IdlingRequest> {
        val matched = filter(items) { candidate: IdlingRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingRequests(store: IdlingRequestStore, query: IdlingRequestQuery): MutableList<IdlingRequest> {
    return query.apply(store.list())
}
