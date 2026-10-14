package com.acme.transport.customs.declaration.batch

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default declaration reviews by code. */
interface DefaultDeclarationReviewStore {
    fun find(code: String): DefaultDeclarationReview?
    fun save(item: DefaultDeclarationReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultDeclarationReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultDeclarationReviewStore(
    var items: MutableMap<String, DefaultDeclarationReview>,
    var order: MutableList<String>,
) : DefaultDeclarationReviewStore {
    override fun find(code: String): DefaultDeclarationReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultDeclarationReview) {
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

    override fun list(): MutableList<DefaultDeclarationReview> {
        val out = mutableListOf<DefaultDeclarationReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultDeclarationReviewStore(): DefaultDeclarationReviewStore {
    return MemoryDefaultDeclarationReviewStore(
        mutableMapOf<String, DefaultDeclarationReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultDeclarationReviewStore(
    var inner: DefaultDeclarationReviewStore,
    var cache: TtlCache<DefaultDeclarationReview>,
    var hits: Int,
    var misses: Int,
) : DefaultDeclarationReviewStore {
    override fun find(code: String): DefaultDeclarationReview? {
        val hit = (cache.get(code) as DefaultDeclarationReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultDeclarationReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultDeclarationReview> = inner.list()
}

fun newCachedDefaultDeclarationReviewStore(inner: DefaultDeclarationReviewStore, ttl: Int, clock: Clock): DefaultDeclarationReviewStore {
    return CachedDefaultDeclarationReviewStore(
        inner = inner,
        cache = newTtlCache<DefaultDeclarationReview>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default declaration reviews. Empty fields match all. */
data class DefaultDeclarationReviewQuery(
    var category: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultDeclarationReview): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultDeclarationReview>): MutableList<DefaultDeclarationReview> {
        val matched = filter(items) { candidate: DefaultDeclarationReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultDeclarationReviews(store: DefaultDeclarationReviewStore, query: DefaultDeclarationReviewQuery): MutableList<DefaultDeclarationReview> {
    return query.apply(store.list())
}
