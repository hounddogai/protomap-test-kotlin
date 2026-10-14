package com.acme.transport.lastmile.attempt.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps default attempt claims by code. */
interface DefaultAttemptClaimStore {
    fun find(code: String): DefaultAttemptClaim?
    fun save(item: DefaultAttemptClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultAttemptClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultAttemptClaimStore(
    var items: MutableMap<String, DefaultAttemptClaim>,
    var order: MutableList<String>,
) : DefaultAttemptClaimStore {
    override fun find(code: String): DefaultAttemptClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultAttemptClaim) {
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

    override fun list(): MutableList<DefaultAttemptClaim> {
        val out = mutableListOf<DefaultAttemptClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultAttemptClaimStore(): DefaultAttemptClaimStore {
    return MemoryDefaultAttemptClaimStore(mutableMapOf<String, DefaultAttemptClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultAttemptClaimStore(
    var inner: DefaultAttemptClaimStore,
    var cache: TtlCache<DefaultAttemptClaim>,
    var hits: Int,
    var misses: Int,
) : DefaultAttemptClaimStore {
    override fun find(code: String): DefaultAttemptClaim? {
        val hit = (cache.get(code) as DefaultAttemptClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultAttemptClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultAttemptClaim> = inner.list()
}

fun newCachedDefaultAttemptClaimStore(inner: DefaultAttemptClaimStore, ttl: Int, clock: Clock): DefaultAttemptClaimStore {
    return CachedDefaultAttemptClaimStore(
        inner = inner,
        cache = newTtlCache<DefaultAttemptClaim>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default attempt claims. Empty fields match all. */
data class DefaultAttemptClaimQuery(
    var segment: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultAttemptClaim): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultAttemptClaim>): MutableList<DefaultAttemptClaim> {
        val matched = filter(items) { candidate: DefaultAttemptClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultAttemptClaims(store: DefaultAttemptClaimStore, query: DefaultAttemptClaimQuery): MutableList<DefaultAttemptClaim> {
    return query.apply(store.list())
}
