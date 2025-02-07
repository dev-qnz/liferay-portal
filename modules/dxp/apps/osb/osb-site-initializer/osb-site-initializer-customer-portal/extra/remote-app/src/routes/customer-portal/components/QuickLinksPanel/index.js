import ClayIcon from '@clayui/icon';
import DOMPurify from 'dompurify';
import {useCallback, useEffect, useState} from 'react';
import {LiferayTheme} from '../../../../common/services/liferay';
import {fetchHeadless} from '../../../../common/services/liferay/api';
import {Storage} from '../../../../common/services/liferay/storage';
import {useCustomerPortal} from '../../context';

const WEB_CONTENT_FOLDER_NAME = 'actions';
const WEB_CONTENT_TEMPLATE_NAME = 'Action Card';
const SITE_GROUP_ID = LiferayTheme.getScopeGroupId();

const QuickLinksPanel = () => {
	const [{quickLinks}] = useCustomerPortal();
	const [expandedPanel, setExpandedPanel] = useState(true);
	const [quickLinksData, setQuickLinksData] = useState([]);

	useEffect(() => {
		setExpandedPanel(
			JSON.parse(Storage.getItem('quick-links-container-expanded'))
		);
	}, []);

	const fetchQuickLinksPanelContent = useCallback(async () => {
		const structuredContentFolders = await fetchHeadless({
			url: `/sites/${SITE_GROUP_ID}/structured-content-folders`,
		});

		const {id: quickLinksInstructionsFolderId} =
			structuredContentFolders.items.find(
				({name}) => name === WEB_CONTENT_FOLDER_NAME
			) || {};

		const contentTemplates = await fetchHeadless({
			url: `/sites/${SITE_GROUP_ID}/content-templates`,
		});

		const contentTemplate = contentTemplates.items.find(
			({name}) => name === WEB_CONTENT_TEMPLATE_NAME
		);

		const structuredContents = await fetchHeadless({
			url: `/structured-content-folders/${quickLinksInstructionsFolderId}/structured-contents`,
		});

		const renderedQuickLinksData = await quickLinks.reduce(
			async (webContentList, webContent) => {
				const promiseStructuredContentList = await webContentList;

				const structuredContent =
					structuredContents?.items.find(
						({friendlyUrlPath, key}) =>
							friendlyUrlPath === webContent ||
							key === webContent.toUpperCase()
					)?.id || {};

				const structuredComponent = await fetchHeadless({
					resolveAsJson: false,
					url: `/structured-contents/${structuredContent}/rendered-content/${contentTemplate?.id}`,
				});

				promiseStructuredContentList.push(
					await structuredComponent.text()
				);

				return webContentList;
			},
			Promise.resolve([])
		);

		setQuickLinksData(renderedQuickLinksData);
	}, [quickLinks]);

	useEffect(() => {
		if (quickLinks) {
			fetchQuickLinksPanelContent();
		}
	}, [quickLinks, fetchQuickLinksPanelContent]);

	return (
		<div>
			<div
				className={`${
					!expandedPanel ? 'position-absolute' : ''
				} link-body mr-4 p-4 quick-links-container rounded`}
			>
				<div className="align-items-center d-flex justify-content-between">
					<h5 className="c-my-0 c-py-2 text-neutral-10">
						Quick Links
					</h5>

					<a
						className="borderless btn c-my-0 c-pr-3 c-py-4 h6 neutral text-neutral-8"
						id="hide-link"
						onClick={() => {
							setExpandedPanel(!expandedPanel);
							Storage.setItem(
								'quick-links-container-expanded',
								JSON.stringify(!expandedPanel)
							);
						}}
					>
						<ClayIcon
							symbol={expandedPanel ? 'hr' : 'order-arrow-left'}
						/>

						<u>{expandedPanel ? 'Hide' : 'Show'}</u>
					</a>
				</div>

				{expandedPanel && (
					<div className="c-pt-3 cp-tip-container">
						{quickLinksData.map((quickLinkContent) => (
							<div
								className="bg-white card-body card-container link-body mb-3 rounded-lg"
								dangerouslySetInnerHTML={{
									__html: DOMPurify.sanitize(
										quickLinkContent,
										{
											USE_PROFILES: {html: true},
										}
									),
								}}
								key={quickLinkContent}
							></div>
						))}
					</div>
				)}
			</div>
		</div>
	);
};

export default QuickLinksPanel;
