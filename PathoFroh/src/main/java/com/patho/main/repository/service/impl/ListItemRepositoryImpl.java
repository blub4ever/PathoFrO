package com.patho.main.repository.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.patho.main.model.ListItem;
import com.patho.main.model.ListItem_;
import com.patho.main.repository.service.ListItemRepositoryCustom;

public class ListItemRepositoryImpl extends AbstractRepositoryCustom implements ListItemRepositoryCustom {

	@Override
	public List<ListItem> findAll(ListItem.StaticList listType, boolean irgnoreArchived) {
		CriteriaQuery<ListItem> criteria = getCriteriaBuilder().createQuery(ListItem.class);
		Root<ListItem> root = criteria.from(ListItem.class);

		criteria.select(root);

		List<Predicate> predicates = new ArrayList<Predicate>();
		predicates.add(getCriteriaBuilder().equal(root.get(ListItem_.listType), listType));

		if (irgnoreArchived)
			predicates.add(getCriteriaBuilder().equal(root.get(ListItem_.archived), false));

		criteria.where(getCriteriaBuilder().and(predicates.toArray(new Predicate[predicates.size()])));

		criteria.distinct(true);

		return getSession().createQuery(criteria).getResultList();
	}

	public List<ListItem> findAllOrderByIndex(ListItem.StaticList listType, boolean irgnoreArchived) {

		CriteriaQuery<ListItem> criteria = getCriteriaBuilder().createQuery(ListItem.class);
		Root<ListItem> root = criteria.from(ListItem.class);

		criteria.select(root);

		List<Predicate> predicates = new ArrayList<Predicate>();
		predicates.add(getCriteriaBuilder().equal(root.get(ListItem_.listType), listType));

		if (irgnoreArchived)
			predicates.add(getCriteriaBuilder().equal(root.get(ListItem_.archived), false));

		criteria.where(getCriteriaBuilder().and(predicates.toArray(new Predicate[predicates.size()])));

		criteria.orderBy(getCriteriaBuilder().asc(root.get(ListItem_.indexInList)));

		criteria.distinct(true);

		return getSession().createQuery(criteria).getResultList();

	}

}
