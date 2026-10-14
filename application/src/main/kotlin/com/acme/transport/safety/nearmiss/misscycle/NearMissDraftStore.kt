package com.acme.transport.safety.nearmiss.misscycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss drafts by code. */
interface NearMissDraftStore {
    fun find(code: String): NearMissDraft?
    fun save(item: NearMissDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissDraftStore(
    var items: MutableMap<String, NearMissDraft>,
    var order: MutableList<String>,
) : NearMissDraftStore {
    override fun find(code: String): NearMissDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissDraft) {
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

    override fun list(): MutableList<NearMissDraft> {
        val out = mutableListOf<NearMissDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissDraftStore(): NearMissDraftStore {
    return MemoryNearMissDraftStore(mutableMapOf<String, NearMissDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissDraftStore(
    var inner: NearMissDraftStore,
    var cache: TtlCache<NearMissDraft>,
    var hits: Int,
    var misses: Int,
) : NearMissDraftStore {
    override fun find(code: String): NearMissDraft? {
        val hit = (cache.get(code) as NearMissDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissDraft> = inner.list()
}

fun newCachedNearMissDraftStore(inner: NearMissDraftStore, ttl: Int, clock: Clock): NearMissDraftStore {
    return CachedNearMissDraftStore(
        inner = inner,
        cache = newTtlCache<NearMissDraft>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss drafts. Empty fields match all. */
data class NearMissDraftQuery(
    var category: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissDraft): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NearMissDraft>): MutableList<NearMissDraft> {
        val matched = filter(items) { candidate: NearMissDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissDrafts(store: NearMissDraftStore, query: NearMissDraftQuery): MutableList<NearMissDraft> {
    return query.apply(store.list())
}
