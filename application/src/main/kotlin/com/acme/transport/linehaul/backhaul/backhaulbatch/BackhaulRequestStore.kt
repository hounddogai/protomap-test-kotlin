package com.acme.transport.linehaul.backhaul.backhaulbatch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul requests by code. */
interface BackhaulRequestStore {
    fun find(code: String): BackhaulRequest?
    fun save(item: BackhaulRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulRequestStore(
    var items: MutableMap<String, BackhaulRequest>,
    var order: MutableList<String>,
) : BackhaulRequestStore {
    override fun find(code: String): BackhaulRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulRequest) {
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

    override fun list(): MutableList<BackhaulRequest> {
        val out = mutableListOf<BackhaulRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulRequestStore(): BackhaulRequestStore {
    return MemoryBackhaulRequestStore(mutableMapOf<String, BackhaulRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulRequestStore(
    var inner: BackhaulRequestStore,
    var cache: TtlCache<BackhaulRequest>,
    var hits: Int,
    var misses: Int,
) : BackhaulRequestStore {
    override fun find(code: String): BackhaulRequest? {
        val hit = (cache.get(code) as BackhaulRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulRequest> = inner.list()
}

fun newCachedBackhaulRequestStore(inner: BackhaulRequestStore, ttl: Int, clock: Clock): BackhaulRequestStore {
    return CachedBackhaulRequestStore(
        inner = inner,
        cache = newTtlCache<BackhaulRequest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul requests. Empty fields match all. */
data class BackhaulRequestQuery(
    var channel: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulRequest): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<BackhaulRequest>): MutableList<BackhaulRequest> {
        val matched = filter(items) { candidate: BackhaulRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulRequests(store: BackhaulRequestStore, query: BackhaulRequestQuery): MutableList<BackhaulRequest> {
    return query.apply(store.list())
}
